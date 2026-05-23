package com.czy.ai.langchain4j.service;

import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.AiChatProviderFactory;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatModelFactory;
import com.czy.ai.langchain4j.chatrequest.LangChainChatRequest;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 聊天服务实现
 * 封装 ChatMemory 原生记忆管理、模型选择、流式回调等业务逻辑
 *
 * @author chenzhenyu
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private AiChatProviderFactory aiChatProviderFactory;

    @Autowired
    private ChatModelFactory chatModelFactory;

    @Autowired
    private ChatMemoryManager chatMemoryManager;

    @Autowired
    @Qualifier("aiTaskExecutor")
    private Executor aiTaskExecutor;

    @Override
    public Result<String> chat(String question, String aiType) {
        AiType type = AiType.safeValueOf(aiType);
        return aiChatProviderFactory.getAiChatProvider(type).chat(question);
    }

    @Override
    public Result<String> customChat(LangChainChatRequest chatRequest) {
        AiType type = AiType.safeValueOf(chatRequest.getAiType());
        return aiChatProviderFactory.getAiChatProvider(type).customChat(chatRequest);
    }

    @Override
    public void streamChat(String question, String aiType, String sessionId, String userId,
                           String systemPrompt, StreamCallback callback) {
        AiType type = AiType.safeValueOf(aiType);
        String finalUserId = (userId != null && !userId.isEmpty()) ? userId : "anonymous";

        aiTaskExecutor.execute(() -> doStream(question, type, sessionId, finalUserId, systemPrompt, callback));
    }

    @Override
    public void streamCustomChat(LangChainChatRequest chatRequest, StreamCallback callback) {
        AiType type = AiType.safeValueOf(chatRequest.getAiType());
        String finalUserId = (chatRequest.getUserId() != null && !chatRequest.getUserId().isEmpty())
                ? chatRequest.getUserId() : "anonymous";

        aiTaskExecutor.execute(() -> doStream(
                chatRequest.getQuestion(), type, chatRequest.getSessionId(),
                finalUserId, chatRequest.getSystemPrompt(), callback));
    }

    /**
     * 流式对话核心逻辑
     */
    private void doStream(String question, AiType aiType, String sessionId,
                          String userId, String systemPrompt, StreamCallback callback) {
        try {
            long startTime = System.currentTimeMillis();

            // 1. 获取/创建 ChatMemory（自动处理 SystemMessage + 消息淘汰）
            ChatMemory chatMemory = chatMemoryManager.getOrCreate(sessionId, userId, systemPrompt);
            String finalSessionId = sessionId;

            // 2. 添加用户消息到 ChatMemory
            chatMemory.add(UserMessage.from(question));

            // 3. 获取流式模型
            StreamingChatModel model = chatModelFactory.getStreamingChatModel(aiType);
            if (model == null) {
                callback.onError(new IllegalStateException("未注册该 AI 类型的流式模型: " + aiType));
                return;
            }

            // 4. 使用 ChatMemory 中的所有消息进行流式调用
            model.chat(chatMemory.messages(), new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    callback.onToken(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    try {
                        // 防御性检查：LangChain4j bug，API 调用失败时 response 可能为 null
                        if (completeResponse == null || completeResponse.aiMessage() == null) {
                            log.warn("流式对话收到空响应 - sessionId: {}, aiType: {}", finalSessionId, aiType);
                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("sessionId", finalSessionId);
                            metadata.put("userId", userId);
                            metadata.put("aiType", aiType.name());
                            callback.onComplete("", metadata);
                            return;
                        }

                        // 将 AI 响应加入 ChatMemory
                        String content = completeResponse.aiMessage().text();
                        chatMemory.add(completeResponse.aiMessage());

                        long costTime = System.currentTimeMillis() - startTime;
                        log.debug("流式对话完成 - sessionId: {}, userId: {}, aiType: {}, 耗时: {}ms",
                                finalSessionId, userId, aiType, costTime);

                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("sessionId", finalSessionId);
                        metadata.put("userId", userId);
                        metadata.put("aiType", aiType.name());
                        metadata.put("costTime", costTime);

                        callback.onComplete(content, metadata);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式对话异常 - sessionId: {}, aiType: {}", finalSessionId, aiType, error);
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            log.error("流式对话执行异常 - aiType: {}", aiType, e);
            callback.onError(e);
        }
    }
}
