package com.czy.ai.langchain4j.controller;

import com.czy.ai.common.annotation.ControllerLog;
import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.chatrequest.LangChainChatRequest;
import com.czy.ai.langchain4j.service.ChatService;
import com.czy.ai.langchain4j.service.StreamCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;

/**
 * LangChain4j 聊天控制器
 * 仅负责参数校验与路由，业务逻辑委托给 ChatService
 *
 * @author chenzhenyu 2026年05月19日 上午10:24:38
 */
@Slf4j
@RestController
@RequestMapping("/ai/langchain")
public class LangChainChatController {

    @Autowired
    private ChatService chatService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * langchain对话
     */
    @GetMapping("/chat")
    @ControllerLog(desc = "langchain 对话")
    public Result<String> chat(@RequestParam("question") String question,
                               @RequestParam("aiType") String aiType) {
        return chatService.chat(question, aiType);
    }

    /**
     * 自定义对话 - 支持完整的聊天参数配置
     */
    @PostMapping("/customChat")
    @ControllerLog(desc = "langchain 自定义对话")
    public Result<String> customChat(@RequestBody LangChainChatRequest chatRequest) {
        if (chatRequest == null || chatRequest.getQuestion() == null) {
            return Result.fail("请求参数不能为空");
        }
        return chatService.customChat(chatRequest);
    }

    /**
     * langchain流式对话
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
        registerSseLifecycle(emitter, sessionId, aiType);

        chatService.streamChat(question, aiType, sessionId, userId, systemPrompt,
                createSseCallback(emitter));

        return emitter;
    }

    /**
     * 自定义对话 - 流式响应
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
        registerSseLifecycle(emitter, chatRequest.getSessionId(), chatRequest.getAiType());

        chatService.streamCustomChat(chatRequest, createSseCallback(emitter));

        return emitter;
    }

    // ==================== 私有方法 ====================

    /**
     * 创建 SseEmitter 的 StreamCallback 适配器
     */
    private StreamCallback createSseCallback(SseEmitter emitter) {
        return new StreamCallback() {
            @Override
            public void onToken(String token) {
                try {
                    emitter.send(SseEmitter.event().data(token));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(String fullContent, Map<String, Object> metadata) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("finish")
                            .data(objectMapper.writeValueAsString(metadata)));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        };
    }

    /**
     * 注册 SseEmitter 生命周期回调，防止资源泄漏
     */
    private void registerSseLifecycle(SseEmitter emitter, String sessionId, String aiType) {
        emitter.onTimeout(() -> log.warn("SSE连接超时: sessionId={}, aiType={}", sessionId, aiType));
        emitter.onError(e -> log.warn("SSE连接异常: sessionId={}, aiType={}", sessionId, aiType, e));
        emitter.onCompletion(() -> log.debug("SSE连接完成: sessionId={}, aiType={}", sessionId, aiType));
    }
}
