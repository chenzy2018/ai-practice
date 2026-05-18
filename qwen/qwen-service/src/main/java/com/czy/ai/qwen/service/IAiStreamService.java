package com.czy.ai.qwen.service;

import com.czy.ai.qwen.common.dto.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 流式服务接口
 * 
 * @author nober
 */
public interface IAiStreamService {

    SseEmitter chatStreamWithContext(ChatRequest request);
}
