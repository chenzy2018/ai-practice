package com.czy.qwen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author chenzhenyu 2026年05月14日 上午10:51:25
 */
@Component
public class QwenConfig {

    @Value("${dashscope.api-key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
