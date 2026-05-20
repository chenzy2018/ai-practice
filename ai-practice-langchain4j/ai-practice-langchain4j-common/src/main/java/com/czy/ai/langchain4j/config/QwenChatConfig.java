package com.czy.ai.langchain4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Qwen AI 配置类
 * 统一管理 AI 相关的配置项，包括 API密钥、URL、HTTP超时、会话管理配置
 *
 * @author nober 2026年05月15日
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.chat.qwen")
public class QwenChatConfig {

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
