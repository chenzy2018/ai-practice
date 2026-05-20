package com.czy.ai.common.session;

import com.czy.ai.common.config.SessionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理器
 * 负责会话的创建、存储、查询和超时清理
 *
 * @author nober 2026年05月14日
 */
@Slf4j
@Component
public class SessionManage {

    @Resource
    private SessionConfig sessionConfig;

    private final ConcurrentHashMap<String, SessionContext> sessionContextMap = new ConcurrentHashMap<>();

    public SessionContext getOrCreate(String sessionId, String userId, String systemPrompt) {
        return sessionContextMap.computeIfAbsent(sessionId, k -> createNewSession(k, userId, systemPrompt));
    }

    public SessionContext get(String sessionId) {
        return sessionContextMap.get(sessionId);
    }

    public SessionContext remove(String sessionId) {
        return sessionContextMap.remove(sessionId);
    }

    public Set<String> getAllSessionIds() {
        return sessionContextMap.keySet();
    }

    public int getSessionCount() {
        return sessionContextMap.size();
    }

    public void updateLastActiveTime(String sessionId) {
        SessionContext context = sessionContextMap.get(sessionId);
        if (context != null) {
            context.updateLastActiveTime();
        }
    }

    @Scheduled(fixedRateString = "#{qwenConfig.session.cleanupIntervalMs}")
    public void cleanupExpiredSessions() {
        long timeoutMillis = TimeUnit.SECONDS.toMillis(sessionConfig.getTimeoutSeconds());
        int[] removedCount = {0};
        int[] totalMessages = {0};

        sessionContextMap.entrySet().removeIf(entry -> {
            SessionContext context = entry.getValue();
            if (context.isExpired(timeoutMillis)) {
                removedCount[0]++;
                totalMessages[0] += context.getMessageCount();
                log.info("清理过期会话 - sessionId: {}, userId: {}, 消息数: {}, 空闲时间: {}秒",
                        entry.getKey(), context.getUserId(), context.getMessageCount(),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - context.getLastActiveTime()));
                return true;
            }
            return false;
        });

        if (removedCount[0] > 0) {
            log.info("会话清理完成 - 共清理 {} 个会话, {} 条消息, 当前剩余: {} 个会话",
                    removedCount[0], totalMessages[0], sessionContextMap.size());
        }

        trimOversizedSessions();
    }

    private void trimOversizedSessions() {
        int maxMessages = sessionConfig.getMaxMessages();
        int[] trimmedCount = {0};
        int[] totalTrimmed = {0};

        for (SessionContext context : sessionContextMap.values()) {
            if (context.getMessageCount() > maxMessages) {
                int trimmed = context.getMessageCount() - maxMessages;
                context.trimMessages(maxMessages);
                trimmedCount[0]++;
                totalTrimmed[0] += trimmed;
            }
        }

        if (trimmedCount[0] > 0) {
            log.info("会话裁剪完成 - 共裁剪 {} 个会话, {} 条消息, 当前最大保留: {} 条",
                    trimmedCount[0], totalTrimmed[0], maxMessages);
        }
    }

    private SessionContext createNewSession(String sessionId, String userId, String systemPrompt) {
        log.info("创建新会话 - sessionId: {}, userId: {}, hasSystemPrompt: {}",
                sessionId, userId, systemPrompt != null && !systemPrompt.trim().isEmpty());
        return SessionContext.builder()
                .sessionId(sessionId)
                .userId(userId)
                .systemPrompt(systemPrompt)
                .build();
    }

    public Map<String, SessionContext> getAllSessions() {
        return new ConcurrentHashMap<>(sessionContextMap);
    }
}
