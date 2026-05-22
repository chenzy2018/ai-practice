package com.czy.ai.langchain4j.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Qwen AI 配置类
 * 继承公共配置，管理 Qwen 特有配置项
 *
 * @author nober 2026年05月15日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "ai.chat.qwen")
public class QwenChatConfig extends AbstractAiChatConfig {
}
