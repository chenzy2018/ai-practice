package com.czy.ai.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel 注册中心
 * 统一管理 ChatModel 和 StreamingChatModel 实例
 *
 * @author chenzhenyu 2026年05月18日 下午22:17:08
 */
@Slf4j
@Component
public class ChatModelFactory {

    private final ConcurrentHashMap<AiType, ChatModel> chatModelMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<AiType, StreamingChatModel> streamingModelMap = new ConcurrentHashMap<>();

    public ChatModel getChatModel(AiType aiType) {
        return chatModelMap.get(aiType);
    }

    public StreamingChatModel getStreamingChatModel(AiType aiType) {
        return streamingModelMap.get(aiType);
    }

    /**
     * @deprecated 使用 {@link #getChatModel(AiType)} 替代
     */
    @Deprecated
    public ChatModel getAiChatProvider(AiType aiType) {
        return getChatModel(aiType);
    }

    /**
     * @deprecated 使用 {@link #getStreamingChatModel(AiType)} 替代
     */
    @Deprecated
    public StreamingChatModel getStreamingChatLanguageModel(AiType aiType) {
        return getStreamingChatModel(aiType);
    }

    public void registerChatModel(AiType aiType, ChatModel chatModel) {
        chatModelMap.put(aiType, chatModel);
        log.info("注册 ChatModel: aiType={}", aiType);
    }

    public void registerStreamingChatModel(AiType aiType, StreamingChatModel streamingChatModel) {
        streamingModelMap.put(aiType, streamingChatModel);
        log.info("注册 StreamingChatModel: aiType={}", aiType);
    }

    /**
     * @deprecated 使用 {@link #registerChatModel(AiType, ChatModel)} 替代
     */
    @Deprecated
    public void registerAiChatProvider(AiType aiType, ChatModel chatModel) {
        registerChatModel(aiType, chatModel);
    }

    /**
     * @deprecated 使用 {@link #registerStreamingChatModel(AiType, StreamingChatModel)} 替代
     */
    @Deprecated
    public void registerStreamingChatProvider(AiType aiType, StreamingChatModel streamingChatModel) {
        registerStreamingChatModel(aiType, streamingChatModel);
    }
}
