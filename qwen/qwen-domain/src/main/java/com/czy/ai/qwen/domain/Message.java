package com.czy.ai.qwen.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 消息 DTO
 *
 * @author chenzhenyu 2026年05月14日
 */
@Getter
public class Message {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";

    private final String role;
    private final String content;

    private Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message user(String content) {
        return new Message(ROLE_USER, content);
    }

    public static Message assistant(String content) {
        return new Message(ROLE_ASSISTANT, content);
    }

    public static Message system(String content) {
        return new Message(ROLE_SYSTEM, content);
    }
}