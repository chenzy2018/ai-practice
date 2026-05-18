package com.czy.ai.qwen.common.config;

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
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenConfig {

    private String apiKey;

    private String apiUrl;

    private OkHttpConfig http;

    private SessionConfig session;

    /**
     * OkHttp HTTP客户端配置
     */
    @Data
    public static class OkHttpConfig {

        /**
         * 连接超时时间（秒）
         */
        private int connectTimeout;

        /**
         * 读取超时时间（秒）
         */
        private int readTimeout;

        /**
         * 写入超时时间（秒）
         */
        private int writeTimeout;

        /**
         * 流式读取超时时间（秒）
         */
        private int streamReadTimeout;
    }

    /**
     * 会话管理配置
     */
    @Data
    public static class SessionConfig {

        /**
         * 会话超时时间（秒）
         */
        private long timeoutSeconds;

        /**
         * 会话清理间隔（毫秒）
         */
        private long cleanupIntervalMs;

        /**
         * 每分钟最大会话数
         */
        private int maxSessionsPerMinute;

        /**
         * 最大保留消息数
         */
        private int maxMessages = 20;
    }
}
