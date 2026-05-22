package com.czy.ai.langchain4j;

import com.google.common.collect.Maps;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author chenzhenyu 2026年05月18日 下午22:17:08
 */
@Component
@Slf4j
public class ChatLanguageModelFactory {

    private static final Map<AiType, ChatLanguageModel> AI_CHAT_PROVIDER_MAP = Maps.newHashMap();
    private static final Map<AiType, StreamingChatLanguageModel> STREAMING_CHAT_PROVIDER_MAP = Maps.newHashMap();

    public ChatLanguageModel getAiChatProvider(AiType aiType) {
        return AI_CHAT_PROVIDER_MAP.get(aiType);
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel(AiType aiType) {
        return STREAMING_CHAT_PROVIDER_MAP.get(aiType);
    }

    public void registerAiChatProvider(AiType aiType, ChatLanguageModel chatLanguageModel) {
        AI_CHAT_PROVIDER_MAP.put(aiType, chatLanguageModel);
    }

    public void registerStreamingChatProvider(AiType aiType, StreamingChatLanguageModel streamingChatLanguageModel) {
        STREAMING_CHAT_PROVIDER_MAP.put(aiType, streamingChatLanguageModel);
    }

}
