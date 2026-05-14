package com.czy.qwen.dto;

/**
 * 消息 DTO
 *
 * @author chenzhenyu 2026年05月14日
 */
public class Message {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";

    private String role;
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}