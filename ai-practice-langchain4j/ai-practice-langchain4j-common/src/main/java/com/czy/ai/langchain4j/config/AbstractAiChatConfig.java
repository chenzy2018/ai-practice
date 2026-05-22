package com.czy.ai.langchain4j.config;

import lombok.Data;

/**
 * AI 聊天配置基类
 * 统一管理各 AI 提供商的公共配置项
 *
 * @author chenzhenyu
 */
@Data
public abstract class AbstractAiChatConfig {

    private String apiKey;
    private String apiUrl;
    private String token;
    private String model;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private String thinkingType;
    private Integer thinkingBudgetTokens;
}
