package com.czy.ai.qwen.service;

import com.czy.ai.qwen.common.dto.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAiStreamService {

    SseEmitter chatStreamWithContext(ChatRequest request);
}