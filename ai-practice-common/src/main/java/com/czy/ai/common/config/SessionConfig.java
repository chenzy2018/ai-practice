package com.czy.ai.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenzhenyu 2026年05月19日 上午11:37:33
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.session")
public class SessionConfig {

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
