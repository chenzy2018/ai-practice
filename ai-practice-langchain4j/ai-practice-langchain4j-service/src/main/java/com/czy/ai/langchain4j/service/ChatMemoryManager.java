package com.czy.ai.langchain4j.service;

import com.czy.ai.common.config.SessionConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ChatMemory 管理器
 * 实现 ChatMemoryProvider 接口，兼容 LangChain4j AiServices 生态
 *
 * 核心能力：
 * - ChatMemoryProvider：为 AiServices 提供按 memoryId 的 ChatMemory 工厂
 * - MessageWindowChatMemory：滑动窗口消息淘汰，自动保护 SystemMessage
 * - ChatMemoryStore 可插拔：后续可接入 Redis/DB 持久化
 * - 会话级生命周期管理：创建、查询、过期清理
 *
 * @author chenzhenyu
 */
@Slf4j
@Component
public class ChatMemoryManager implements ChatMemoryProvider {

    @Autowired
    private SessionConfig sessionConfig;

    private final ConcurrentHashMap<String, ChatMemoryEntry> memoryMap = new ConcurrentHashMap<>();

    /**
     * ChatMemoryProvider 接口实现
     * 为 AiServices 提供按 memoryId 获取/创建 ChatMemory 的能力
     * memoryId 即 sessionId，AiServices 通过 @MemoryId 注解自动传入
     */
    @Override
    public ChatMemory get(Object memoryId) {
        return getOrCreate((String) memoryId, (String) memoryId, null);
    }

    /**
     * 获取指定 memoryId 的 systemPrompt
     * 用于 AiServices 的 systemMessageProvider 回调
     */
    public String getSystemPrompt(Object memoryId) {
        ChatMemoryEntry entry = memoryMap.get(memoryId);
        return entry != null ? entry.getSystemPrompt() : null;
    }

    /**
     * 获取或创建 ChatMemory
     *
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @param systemPrompt 系统提示词
     * @return ChatMemory 实例
     */
    public ChatMemory getOrCreate(String sessionId, String userId, String systemPrompt) {
        ChatMemoryEntry entry = memoryMap.computeIfAbsent(sessionId, k -> {
            log.info("创建新 ChatMemory 会话 - sessionId: {}, userId: {}", k, userId);

            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .id(k)
                    .maxMessages(sessionConfig.getMaxMessages())
                    .build();

            // ChatMemory 自动处理 SystemMessage：唯一、保留、去重
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                chatMemory.add(SystemMessage.from(systemPrompt));
            }

            return new ChatMemoryEntry(chatMemory, userId, systemPrompt);
        });

        entry.updateLastActiveTime();
        return entry.getChatMemory();
    }

    /**
     * 获取已有 ChatMemory（不创建）
     */
    public ChatMemory get(String sessionId) {
        ChatMemoryEntry entry = memoryMap.get(sessionId);
        return entry != null ? entry.getChatMemory() : null;
    }

    /**
     * 添加用户消息
     */
    public void addUserMessage(String sessionId, String content) {
        ChatMemoryEntry entry = memoryMap.get(sessionId);
        if (entry != null) {
            entry.getChatMemory().add(UserMessage.from(content));
            entry.updateLastActiveTime();
        }
    }

    /**
     * 添加 AI 消息（文本）
     */
    public void addAiMessage(String sessionId, String content) {
        ChatMemoryEntry entry = memoryMap.get(sessionId);
        if (entry != null) {
            entry.getChatMemory().add(AiMessage.from(content));
            entry.updateLastActiveTime();
        }
    }

    /**
     * 添加 AI 消息（AiMessage 对象，用于流式响应完成后直接添加）
     */
    public void addAiMessage(String sessionId, AiMessage aiMessage) {
        ChatMemoryEntry entry = memoryMap.get(sessionId);
        if (entry != null) {
            entry.getChatMemory().add(aiMessage);
            entry.updateLastActiveTime();
        }
    }

    /**
     * 移除会话
     */
    public ChatMemory remove(String sessionId) {
        ChatMemoryEntry entry = memoryMap.remove(sessionId);
        return entry != null ? entry.getChatMemory() : null;
    }

    /**
     * 获取当前会话数
     */
    public int getSessionCount() {
        return memoryMap.size();
    }

    /**
     * 定时清理过期会话
     */
    @Scheduled(fixedRateString = "${ai.session.cleanup-interval-ms:60000}")
    public void cleanupExpiredSessions() {
        long timeoutMillis = TimeUnit.SECONDS.toMillis(sessionConfig.getTimeoutSeconds());
        int[] removedCount = {0};

        memoryMap.entrySet().removeIf(entry -> {
            ChatMemoryEntry context = entry.getValue();
            if (context.isExpired(timeoutMillis)) {
                removedCount[0]++;
                log.info("清理过期 ChatMemory 会话 - sessionId: {}, userId: {}, 消息数: {}, 空闲时间: {}秒",
                        entry.getKey(), context.getUserId(), context.getChatMemory().messages().size(),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - context.getLastActiveTime()));
                return true;
            }
            return false;
        });

        if (removedCount[0] > 0) {
            log.info("ChatMemory 清理完成 - 共清理 {} 个会话, 当前剩余: {} 个会话",
                    removedCount[0], memoryMap.size());
        }
    }

    /**
     * ChatMemory 会话条目
     */
    @Getter
    private static class ChatMemoryEntry {
        private final ChatMemory chatMemory;
        private final String userId;
        private final String systemPrompt;
        private volatile long lastActiveTime;

        ChatMemoryEntry(ChatMemory chatMemory, String userId, String systemPrompt) {
            this.chatMemory = chatMemory;
            this.userId = userId;
            this.systemPrompt = systemPrompt;
            this.lastActiveTime = System.currentTimeMillis();
        }

        void updateLastActiveTime() {
            this.lastActiveTime = System.currentTimeMillis();
        }

        boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - lastActiveTime > timeoutMillis;
        }
    }
}
