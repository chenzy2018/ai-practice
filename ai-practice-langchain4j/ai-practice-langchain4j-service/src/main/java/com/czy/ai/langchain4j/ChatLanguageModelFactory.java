package com.czy.ai.langchain4j;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatLanguageModel 注册中心
 * 统一管理 ChatLanguageModel 和 StreamingChatLanguageModel 实例
 *
 * @author chenzhenyu 2026年05月18日 下午22:17:08
 */
// @Deprecated
@Slf4j
@Component
public class ChatLanguageModelFactory {

    private final ConcurrentHashMap<AiType, ChatLanguageModel> chatModelMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<AiType, StreamingChatLanguageModel> streamingModelMap = new ConcurrentHashMap<>();

    public ChatLanguageModel getChatModel(AiType aiType) {
        return chatModelMap.get(aiType);
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel(AiType aiType) {
        return streamingModelMap.get(aiType);
    }

    public void registerAiChatProvider(AiType aiType, ChatLanguageModel chatLanguageModel) {
        chatModelMap.put(aiType, chatLanguageModel);
        log.info("注册 ChatLanguageModel: aiType={}", aiType);
    }

    public void registerStreamingChatProvider(AiType aiType, StreamingChatLanguageModel streamingChatLanguageModel) {
        streamingModelMap.put(aiType, streamingChatLanguageModel);
        log.info("注册 StreamingChatLanguageModel: aiType={}", aiType);
    }

}
