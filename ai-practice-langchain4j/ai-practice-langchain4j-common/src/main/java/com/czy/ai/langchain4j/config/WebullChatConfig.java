package com.czy.ai.langchain4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenzhenyu 2026年05月20日 下午16:03:48
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.chat.webull")
public class WebullChatConfig {
    private String apiKey;
    private String apiUrl;
    private String token;
    private String model;
    private Boolean stream;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private String thinkingType;
    private Integer thinkingBudgetTokens;
}
