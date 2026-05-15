package com.czy.qwen.server;

import com.czy.qwen.facade.Message;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 会话上下文
 * 包含消息历史、最后活跃时间、systemPrompt
 *
 * @author chenzhenyu 2026年05月14日
 */
@Getter
public class SessionContext {

    private final String sessionId;
    private final String userId;
    private final List<Message> messages;
    private final String systemPrompt;
    private volatile long lastActiveTime;

    @Builder
    public SessionContext(String sessionId, String userId, String systemPrompt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.messages = new CopyOnWriteArrayList<>();
        this.systemPrompt = systemPrompt;
        this.lastActiveTime = System.currentTimeMillis();
        initSystemPrompt();
    }

    private void initSystemPrompt() {
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(Message.system(systemPrompt));
        }
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - lastActiveTime > timeoutMillis;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public void addUserMessage(String content) {
        messages.add(Message.user(content));
    }

    public void addAssistantMessage(String content) {
        messages.add(Message.assistant(content));
    }
}