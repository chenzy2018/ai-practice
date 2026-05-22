package com.czy.ai.langchain4j.provider;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.config.AbstractAiChatConfig;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import com.czy.ai.langchain4j.config.WebullChatConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 动态模型工厂 - 根据请求参数动态创建 ChatLanguageModel
 * 支持缓存复用（Guava Cache，1小时过期，最大50实例），避免重复创建
 * 支持多种 AI 类型（QWEN、WEBULL）
 *
 * @author chenzhenyu
 */
@Slf4j
@Component
public class DynamicModelFactory {

    @Autowired
    private WebullChatConfig webullChatConfig;

    @Autowired
    private QwenChatConfig qwenChatConfig;

    // Guava Cache: 1小时未访问过期，最大缓存50个实例
    private final Cache<String, ChatLanguageModel> chatModelCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final Cache<String, StreamingChatLanguageModel> streamingModelCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    /**
     * 根据请求参数动态创建 ChatLanguageModel（带缓存）
     */
    public ChatLanguageModel createChatModel(ChatRequest request, AiType aiType) {
        String cacheKey = buildCacheKey(request, aiType);
        return chatModelCache.getIfPresent(cacheKey) != null
                ? chatModelCache.getIfPresent(cacheKey)
                : chatModelCache.asMap().computeIfAbsent(cacheKey, key -> {
            log.debug("创建新的 ChatLanguageModel: {}", key);
            return buildChatModel(request, getConfig(aiType));
        });
    }

    /**
     * 根据请求参数动态创建 StreamingChatLanguageModel（带缓存）
     */
    public StreamingChatLanguageModel createStreamingChatModel(ChatRequest request, AiType aiType) {
        String cacheKey = buildCacheKey(request, aiType);
        return streamingModelCache.getIfPresent(cacheKey) != null
                ? streamingModelCache.getIfPresent(cacheKey)
                : streamingModelCache.asMap().computeIfAbsent(cacheKey, key -> {
            log.debug("创建新的 StreamingChatLanguageModel: {}", key);
            return buildStreamingModel(request, getConfig(aiType));
        });
    }

    /**
     * 通用构建 ChatLanguageModel
     */
    private ChatLanguageModel buildChatModel(ChatRequest request, AbstractAiChatConfig config) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getApiUrl())
                .modelName(getModel(request, config.getModel()))
                .temperature(getTemperature(request, config.getTemperature()))
                .maxTokens(getMaxTokens(request, config.getMaxTokens()));

        Double topP = getTopP(request, config.getTopP());
        if (topP != null) {
            builder.topP(topP);
        }

        return builder.build();
    }

    /**
     * 通用构建 StreamingChatLanguageModel
     */
    private StreamingChatLanguageModel buildStreamingModel(ChatRequest request, AbstractAiChatConfig config) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getApiUrl())
                .modelName(getModel(request, config.getModel()))
                .temperature(getTemperature(request, config.getTemperature()))
                .maxTokens(getMaxTokens(request, config.getMaxTokens()));

        Double topP = getTopP(request, config.getTopP());
        if (topP != null) {
            builder.topP(topP);
        }

        return builder.build();
    }

    /**
     * 根据 AiType 获取对应配置
     */
    private AbstractAiChatConfig getConfig(AiType aiType) {
        return AiType.QWEN.equals(aiType) ? qwenChatConfig : webullChatConfig;
    }

    /**
     * 构建缓存键（包含 AI 类型和模型参数）
     */
    private String buildCacheKey(ChatRequest request, AiType aiType) {
        AbstractAiChatConfig config = getConfig(aiType);
        return String.format("%s_%s_%s_%s_%s",
                aiType.name(),
                getModel(request, config.getModel()),
                getTemperature(request, config.getTemperature()),
                getMaxTokens(request, config.getMaxTokens()),
                getTopP(request, config.getTopP())
        );
    }

    private String getModel(ChatRequest request, String defaultModel) {
        return StringUtils.isEmpty(request.getModel()) ? defaultModel : request.getModel();
    }

    private Double getTemperature(ChatRequest request, Double defaultTemp) {
        return request.getTemperature() != null ? request.getTemperature() : defaultTemp;
    }

    private Integer getMaxTokens(ChatRequest request, Integer defaultMax) {
        return request.getMaxTokens() != null ? request.getMaxTokens() : defaultMax;
    }

    private Double getTopP(ChatRequest request, Double defaultTopP) {
        return request.getTopP() != null ? request.getTopP() : (defaultTopP != null ? defaultTopP : null);
    }
}
