package com.czy.qwen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 聊天请求参数
 *
 * @author chenzhenyu 2026年05月14日
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String sessionId;
    private String question;
    private String systemPrompt;
    private String model;
    private Double temperature;
}