package com.czy.ai.qwen.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenConfig {
    private String apiKey;
    private String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private int connectTimeout = 30;
    private int readTimeout = 60;
}