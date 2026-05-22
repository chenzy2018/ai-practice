package com.czy.ai.langchain4j.controller;

import com.czy.ai.common.Message;
import com.czy.ai.common.annotation.ControllerLog;
import com.czy.ai.common.dto.Result;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.common.session.SessionManage;
import com.czy.ai.langchain4j.AiChatProviderFactory;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.chatrequest.LangChainChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author chenzhenyu 2026年05月19日 上午10:24:38
 */
@Slf4j
@RestController
@RequestMapping("/ai/langchain")
public class LangChainChatController {

    @Autowired
    private AiChatProviderFactory aiChatProviderFactory;

    @Autowired
    private ChatLanguageModelFactory chatLanguageModelFactory;

    @Autowired
    private SessionManage sessionManage;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * langchain对话
     *
     * @param question    问题
     * @param aiType   ai类型
     * @return AI 回复
     */
    @GetMapping("/chat")
    @ControllerLog(desc = "langchain 对话")
    public Result<String> chat(@RequestParam("question") String question,
                               @RequestParam("aiType") String aiType) {
        return aiChatProviderFactory.getAiChatProvider(AiType.valueOf(aiType)).chat(question);
    }

    /**
     * langchain流式对话
     *
     * @param question    问题
     * @param aiType      ai类型
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @param systemPrompt 系统提示
     * @return SSE 流式响应
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ControllerLog(desc = "langchain 流式对话")
    public SseEmitter streamChat(@RequestParam("question") String question,
                                 @RequestParam("aiType") String aiType,
                                 @RequestParam(value = "sessionId", required = false) String sessionId,
                                 @RequestParam(value = "userId", required = false) String userId,
                                 @RequestParam(value = "systemPrompt", required = false) String systemPrompt) {
        if (question == null || question.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new IllegalArgumentException("问题不能为空"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(300000L);

        final String finalUserId = (userId != null && !userId.isEmpty()) ? userId : "anonymous";
        long startTime = System.currentTimeMillis();

        log.info("========================================");
        log.info("[Controller 入参] langchain 流式对话");
        log.info("请求路径: GET /ai/langchain/chat/stream");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("question", question);
        paramMap.put("aiType", aiType);
        paramMap.put("sessionId", sessionId);
        paramMap.put("userId", finalUserId);
        paramMap.put("systemPrompt", systemPrompt);
        try {
            log.info("方法参数: {}", objectMapper.writeValueAsString(paramMap));
        } catch (Exception e) {
            log.info("方法参数: {}", paramMap);
        }
        log.info("========================================");

        CompletableFuture.runAsync(() -> {
            try {
                SessionContext session = sessionManage.getOrCreate(sessionId, finalUserId, systemPrompt);
                final String finalSessionId = session.getSessionId();
                sessionManage.updateLastActiveTime(finalSessionId);

                AiType type = AiType.valueOf(aiType);
                StreamingChatLanguageModel model = chatLanguageModelFactory.getStreamingChatLanguageModel(type);

                // 构建消息列表（包含历史上下文）
                List<ChatMessage> messages = buildMessageList(session.getMessages(), question, systemPrompt);

                model.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        try {
                            String content = response.content().text();
                            session.addUserMessage(question);
                            session.addAssistantMessage(content);

                            long costTime = System.currentTimeMillis() - startTime;
                            log.info("========================================");
                            log.info("[Controller 出参] langchain 流式对话");
                            log.info("会话信息 - sessionId: {}, userId: {}, aiType: {}", finalSessionId, finalUserId, aiType);
                            log.info("耗时: {}ms", costTime);
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("sessionId", finalSessionId);
                            resultMap.put("userId", finalUserId);
                            resultMap.put("aiType", aiType);
                            resultMap.put("content", content);
                            resultMap.put("costTime", costTime);
                            try {
                                log.info("出参: {}", objectMapper.writeValueAsString(resultMap));
                            } catch (Exception e) {
                                log.info("出参: {}", resultMap);
                            }
                            log.info("========================================");

                            emitter.send(SseEmitter.event()
                                    .name("finish")
                                    .data(objectMapper.writeValueAsString(resultMap)));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 自定义对话 - 支持完整的聊天参数配置
     *
     * @param chatRequest 自定义聊天请求
     * @return AI 回复
     */
    @PostMapping("/customChat")
    @ControllerLog(desc = "langchain 自定义对话")
    public Result<String> customChat(@RequestBody LangChainChatRequest chatRequest) {
        if (chatRequest == null || chatRequest.getQuestion() == null) {
            return Result.fail("请求参数不能为空");
        }

        AiType aiType = AiType.valueOf(chatRequest.getAiType());
        return aiChatProviderFactory.getAiChatProvider(aiType).customChat(chatRequest);
    }

    /**
     * 自定义对话 - 流式响应
     *
     * @param chatRequest 自定义聊天请求
     * @return SSE 流式响应
     */
    @PostMapping(value = "/customChat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ControllerLog(desc = "langchain 自定义对话（流式）")
    public SseEmitter customChatStream(@RequestBody LangChainChatRequest chatRequest) {
        if (chatRequest == null || chatRequest.getQuestion() == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new IllegalArgumentException("请求参数不能为空"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(300000L);

        long startTime = System.currentTimeMillis();

        log.info("========================================");
        log.info("[Controller 入参] langchain 自定义对话（流式）");
        log.info("请求路径: POST /ai/langchain/customChat/stream");
        try {
            log.info("方法参数: {}", objectMapper.writeValueAsString(chatRequest));
        } catch (Exception e) {
            log.info("方法参数: {}", chatRequest);
        }
        log.info("========================================");

        CompletableFuture.runAsync(() -> {
            try {
                SessionContext session = sessionManage.getOrCreate(
                        chatRequest.getSessionId(),
                        chatRequest.getUserId(),
                        chatRequest.getSystemPrompt()
                );
                sessionManage.updateLastActiveTime(session.getSessionId());

                AiType aiType = AiType.valueOf(chatRequest.getAiType());
                StreamingChatLanguageModel streamingModel = chatLanguageModelFactory.getStreamingChatLanguageModel(aiType);

                // 构建消息列表（包含历史上下文）
                List<ChatMessage> messages = buildMessageList(
                        session.getMessages(),
                        chatRequest.getQuestion(),
                        chatRequest.getSystemPrompt()
                );

                streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        try {
                            String content = response.content().text();
                            session.addUserMessage(chatRequest.getQuestion());
                            session.addAssistantMessage(content);

                            long costTime = System.currentTimeMillis() - startTime;
                            log.info("========================================");
                            log.info("[Controller 出参] langchain 自定义对话（流式）");
                            log.info("会话信息 - sessionId: {}, userId: {}, aiType: {}", session.getSessionId(), session.getUserId(), chatRequest.getAiType());
                            log.info("耗时: {}ms", costTime);
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("sessionId", session.getSessionId());
                            resultMap.put("userId", session.getUserId());
                            resultMap.put("aiType", chatRequest.getAiType());
                            resultMap.put("content", content);
                            resultMap.put("costTime", costTime);
                            try {
                                log.info("出参: {}", objectMapper.writeValueAsString(resultMap));
                            } catch (Exception e) {
                                log.info("出参: {}", resultMap);
                            }
                            log.info("========================================");

                            emitter.send(SseEmitter.event()
                                    .name("finish")
                                    .data(objectMapper.writeValueAsString(resultMap)));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 构建消息列表（包含历史上下文）
     *
     * @param messages     历史消息列表
     * @param question     当前问题
     * @param systemPrompt 系统提示
     * @return LangChain4j 的 ChatMessage 列表
     */
    private List<ChatMessage> buildMessageList(List<Message> messages, String question, String systemPrompt) {
        List<ChatMessage> chatMessages = new ArrayList<>();

        // 1. 添加系统提示（如果有）
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            chatMessages.add(SystemMessage.from(systemPrompt));
        }

        // 2. 添加历史消息
        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                switch (message.getRole()) {
                    case Message.ROLE_USER:
                        chatMessages.add(UserMessage.from(message.getContent()));
                        break;
                    case Message.ROLE_ASSISTANT:
                        chatMessages.add(AiMessage.from(message.getContent()));
                        break;
                    case Message.ROLE_SYSTEM:
                        chatMessages.add(SystemMessage.from(message.getContent()));
                        break;
                    default:
                        chatMessages.add(UserMessage.from(message.getContent()));
                }
            }
        }

        // 3. 添加当前问题
        chatMessages.add(UserMessage.from(question));

        return chatMessages;
    }
}
