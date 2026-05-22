package com.czy.ai.langchain4j.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Webull AI 配置类
 * 继承公共配置，管理 Webull 特有配置项
 *
 * @author chenzhenyu 2026年05月20日 下午16:03:48
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "ai.chat.webull")
public class WebullChatConfig extends AbstractAiChatConfig {

    /**
     * 是否流式
     */
    private Boolean stream;
}
