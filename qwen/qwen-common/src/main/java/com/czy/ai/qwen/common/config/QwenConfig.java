package com.czy.ai.qwen.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Qwen AI 配置类
 * 统一管理 AI 相关的配置项，包括 API密钥、URL、HTTP超时、会话管理配置
 *
 * @author chenzhenyu 2026年05月15日
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenConfig {

    private String apiKey;
    private String apiUrl;
    private OkHttpConfig http;
    private SessionConfig session;

    /**
     * OkHttp HTTP客户端配置
     * 包含连接超时、读取超时、写入超时、流式读取超时配置
     */
    @Data
    public static class OkHttpConfig {
        private int connectTimeout;
        private int readTimeout;
        private int writeTimeout;
        private int streamReadTimeout;
    }

    /**
     * 会话管理配置
     * 包含会话超时时间、清理间隔、每分钟最大会话数配置
     */
    @Data
    public static class SessionConfig {
        private long timeoutSeconds;
        private long cleanupIntervalMs;
        private int maxSessionsPerMinute;
    }
}
