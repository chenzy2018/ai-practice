package com.czy.ai.common.session;

import com.czy.ai.common.Message;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        updateLastActiveTime();
    }

    public void addAssistantMessage(String content) {
        messages.add(Message.assistant(content));
        updateLastActiveTime();
    }

    public void trimMessages(int maxSize) {
        if (messages.size() <= maxSize) {
            return;
        }
        // 保护首条 system prompt，从第二条消息开始裁剪
        int startIndex = !messages.isEmpty() && Message.ROLE_SYSTEM.equals(messages.get(0).getRole()) ? 1 : 0;
        while (messages.size() > maxSize && startIndex < messages.size()) {
            messages.remove(startIndex);
        }
    }
}