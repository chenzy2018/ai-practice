package com.czy.ai.langchain4j.provider;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import com.czy.ai.langchain4j.config.WebullChatConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态模型工厂 - 根据请求参数动态创建 ChatLanguageModel
 * 支持缓存复用，避免重复创建
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

    // 使用 ConcurrentHashMap 作为缓存
    private final Map<String, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatLanguageModel> streamingModelCache = new ConcurrentHashMap<>();

    /**
     * 根据请求参数动态创建 ChatLanguageModel
     */
    public ChatLanguageModel createChatModel(ChatRequest request, AiType aiType) {
        String cacheKey = buildCacheKey(request, aiType);
        
        return chatModelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("创建新的 ChatLanguageModel: {}", key);
            
            // 根据 AI 类型获取配置
            if (AiType.QWEN.equals(aiType)) {
                return createQwenChatModel(request);
            } else {
                return createWebullChatModel(request);
            }
        });
    }

    /**
     * 根据请求参数动态创建 StreamingChatLanguageModel
     */
    public StreamingChatLanguageModel createStreamingChatModel(ChatRequest request, AiType aiType) {
        String cacheKey = buildCacheKey(request, aiType);
        
        return streamingModelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("创建新的 StreamingChatLanguageModel: {}", key);
            
            // 根据 AI 类型获取配置
            if (AiType.QWEN.equals(aiType)) {
                return createQwenStreamingModel(request);
            } else {
                return createWebullStreamingModel(request);
            }
        });
    }

    /**
     * 创建 Qwen ChatLanguageModel
     */
    private ChatLanguageModel createQwenChatModel(ChatRequest request) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(qwenChatConfig.getApiKey())
                .baseUrl(qwenChatConfig.getApiUrl())
                .modelName(getModel(request, qwenChatConfig.getModel()))
                .temperature(getTemperature(request, qwenChatConfig.getTemperature()))
                .maxTokens(getMaxTokens(request, qwenChatConfig.getMaxTokens()));

        if (request.getTopP() != null) {
            builder.topP(request.getTopP());
        } else if (qwenChatConfig.getTopP() != null) {
            builder.topP(qwenChatConfig.getTopP());
        }

        return builder.build();
    }

    /**
     * 创建 Qwen StreamingChatLanguageModel
     */
    private StreamingChatLanguageModel createQwenStreamingModel(ChatRequest request) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(qwenChatConfig.getApiKey())
                .baseUrl(qwenChatConfig.getApiUrl())
                .modelName(getModel(request, qwenChatConfig.getModel()))
                .temperature(getTemperature(request, qwenChatConfig.getTemperature()))
                .maxTokens(getMaxTokens(request, qwenChatConfig.getMaxTokens()));

        if (request.getTopP() != null) {
            builder.topP(request.getTopP());
        } else if (qwenChatConfig.getTopP() != null) {
            builder.topP(qwenChatConfig.getTopP());
        }

        return builder.build();
    }

    /**
     * 创建 Webull ChatLanguageModel
     */
    private ChatLanguageModel createWebullChatModel(ChatRequest request) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(webullChatConfig.getApiKey())
                .baseUrl(webullChatConfig.getApiUrl())
                .modelName(getModel(request, webullChatConfig.getModel()))
                .temperature(getTemperature(request, webullChatConfig.getTemperature()))
                .maxTokens(getMaxTokens(request, webullChatConfig.getMaxTokens()));

        if (request.getTopP() != null) {
            builder.topP(request.getTopP());
        } else if (webullChatConfig.getTopP() != null) {
            builder.topP(webullChatConfig.getTopP());
        }

        return builder.build();
    }

    /**
     * 创建 Webull StreamingChatLanguageModel
     */
    private StreamingChatLanguageModel createWebullStreamingModel(ChatRequest request) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(webullChatConfig.getApiKey())
                .baseUrl(webullChatConfig.getApiUrl())
                .modelName(getModel(request, webullChatConfig.getModel()))
                .temperature(getTemperature(request, webullChatConfig.getTemperature()))
                .maxTokens(getMaxTokens(request, webullChatConfig.getMaxTokens()));

        if (request.getTopP() != null) {
            builder.topP(request.getTopP());
        } else if (webullChatConfig.getTopP() != null) {
            builder.topP(webullChatConfig.getTopP());
        }

        return builder.build();
    }

    /**
     * 构建缓存键（包含 AI 类型）
     */
    private String buildCacheKey(ChatRequest request, AiType aiType) {
        return String.format("%s_%s_%s_%s_%s",
                aiType.name(),
                getModel(request, getDefaultModel(aiType)),
                getTemperature(request, getDefaultTemperature(aiType)),
                getMaxTokens(request, getDefaultMaxTokens(aiType)),
                getTopP(request, getDefaultTopP(aiType))
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
        return request.getTopP() != null ? request.getTopP() : defaultTopP;
    }

    private String getDefaultModel(AiType aiType) {
        return AiType.QWEN.equals(aiType) ? qwenChatConfig.getModel() : webullChatConfig.getModel();
    }

    private Double getDefaultTemperature(AiType aiType) {
        return AiType.QWEN.equals(aiType) ? qwenChatConfig.getTemperature() : webullChatConfig.getTemperature();
    }

    private Integer getDefaultMaxTokens(AiType aiType) {
        return AiType.QWEN.equals(aiType) ? qwenChatConfig.getMaxTokens() : webullChatConfig.getMaxTokens();
    }

    private Double getDefaultTopP(AiType aiType) {
        Double topP = AiType.QWEN.equals(aiType) ? qwenChatConfig.getTopP() : webullChatConfig.getTopP();
        return topP != null ? topP : 1.0;
    }
}
