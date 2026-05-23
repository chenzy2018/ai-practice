package com.czy.ai.langchain4j.service;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

/**
 * AiAssistant 配置类
 * 为每个 AiType 构建带 ChatMemoryProvider 的 AiAssistant 实例
 *
 * AiServices 自动编排：
 * - @MemoryId → ChatMemoryProvider.get(memoryId) → 获取/创建 ChatMemory
 * - @UserMessage → 自动添加 UserMessage 到 ChatMemory
 * - 调用 ChatModel.chat(chatMemory.messages())
 * - 自动添加 AiMessage 到 ChatMemory
 *
 * @author chenzhenyu
 */
@Slf4j
@Configuration
public class AiAssistantConfiguration {

    @Autowired
    private ChatModelFactory chatModelFactory;

    @Autowired
    private ChatMemoryManager chatMemoryManager;

    /**
     * 注册 AiAssistant Bean Map
     * 每个 AiType 对应一个 AiAssistant 实例（绑定对应 ChatModel + ChatMemoryProvider）
     */
    @Bean
    public Map<AiType, AiAssistant> aiAssistantMap() {
        Map<AiType, AiAssistant> map = new EnumMap<>(AiType.class);

        for (AiType aiType : AiType.values()) {
            ChatModel chatModel = chatModelFactory.getChatModel(aiType);
            if (chatModel != null) {
                AiAssistant assistant = AiServices.builder(AiAssistant.class)
                        .chatModel(chatModel)
                        .chatMemoryProvider(chatMemoryManager)
                        .systemMessageProvider(memoryId ->
                                chatMemoryManager.getSystemPrompt(memoryId))
                        .build();

                map.put(aiType, assistant);
                log.info("注册 AiAssistant - aiType: {}", aiType);
            }
        }

        return map;
    }
}
