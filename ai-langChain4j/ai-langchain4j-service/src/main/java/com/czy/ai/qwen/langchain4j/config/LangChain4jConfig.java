package com.czy.ai.qwen.langchain4j.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 *
 * @author nober
 */
@Configuration
public class LangChain4jConfig {

    /**
     * API Key
     */
    @Value("${ai.langchain4j.api-key}")
    private String apiKey;

    @Value("${ai.langchain4j.base-url}")
    private String baseUrl;

    /**
     * 模型名称
     */
    @Value("${ai.langchain4j.model-name}")
    private String modelName;

    /**
     * 温度参数（0-1）
     */
    @Value("${ai.langchain4j.temperature}")
    private double temperature;

    /**
     * 最大输出令牌数
     */
    @Value("${ai.langchain4j.max-tokens}")
    private int maxTokens;

    /**
     * 注入通义千问对话模型
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
}
