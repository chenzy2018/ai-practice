package com.czy.qwen.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理器
 * 负责会话的创建、存储、查询和超时清理
 *
 * @author chenzhenyu 2026年05月14日
 */
@Slf4j
@Component
public class SessionManager {

    @Value("${ai.session.timeout-seconds:1800}")
    private int sessionTimeoutSeconds;

    private final ConcurrentHashMap<String, SessionContext> sessionContextMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("SessionManager 初始化完成，会话超时时间: {} 秒", sessionTimeoutSeconds);
    }

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

    @Scheduled(fixedRateString = "${ai.session.cleanup-interval-ms:60000}")
    public void cleanupExpiredSessions() {
        long timeoutMillis = TimeUnit.SECONDS.toMillis(sessionTimeoutSeconds);
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