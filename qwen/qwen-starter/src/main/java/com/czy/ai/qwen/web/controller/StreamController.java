package com.czy.ai.qwen.web.controller;

import com.czy.ai.qwen.common.annotation.ControllerLog;
import com.czy.ai.qwen.common.annotation.RateLimit;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.service.IAiStreamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * AI 流式对话 Controller
 * 提供 SSE 流式响应风格的对话接口
 *
 * @author chenzhenyu 2026年05月15日
 */
@RestController
@RequestMapping("/ai/stream")
public class StreamController {

    @Resource
    private IAiStreamService streamService;

    /**
     * 流式对话
     * 使用 SSE 进行流式响应
     *
     * @param request     对话请求
     * @param httpRequest HTTP请求
     * @param response    HTTP响应
     * @return SseEmitter 用于流式推送
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit
    // @ControllerLog(desc = "流式对话")
    public SseEmitter chatStream(@RequestBody ChatRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = httpRequest.getRemoteAddr();
        }
        request.setUserId(userId);

        return streamService.chatStreamWithContext(request);
    }
}
