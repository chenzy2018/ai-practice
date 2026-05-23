package com.czy.ai.langchain4j.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * AI 对话助手接口（高阶 API）
 * 基于 LangChain4j 1.0.1 AiServices 声明式编程
 *
 * 通过 @MemoryId 实现多用户/多会话记忆隔离
 * 通过 @UserMessage 传入用户输入
 * AiServices 自动编排：UserMessage → ChatModel → AiMessage → ChatMemory
 *
 * @author chenzhenyu
 */
public interface AiAssistant {

    /**
     * 带记忆的对话
     * AiServices 自动完成：添加 UserMessage → 调用 ChatModel → 添加 AiMessage 到 ChatMemory
     *
     * @param memoryId    会话标识（对应 sessionId），AiServices 自动路由到对应的 ChatMemory
     * @param userMessage 用户消息
     * @return AI 回复文本
     */
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
