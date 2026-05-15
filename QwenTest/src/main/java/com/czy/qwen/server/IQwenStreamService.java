package com.czy.qwen.server;

import com.czy.qwen.req.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Qwen AI 流式服务接口
 *
 * @author chenzhenyu 2026年05月15日
 */
public interface IQwenStreamService {

    /**
     * 流式多轮对话（带上下文）
     *
     * @param request 聊天请求参数，包含 sessionId, question, systemPrompt, model, temperature
     * @return SseEmitter 用于推送流式响应
     */
    SseEmitter chatStreamWithContext(ChatRequest request);
}