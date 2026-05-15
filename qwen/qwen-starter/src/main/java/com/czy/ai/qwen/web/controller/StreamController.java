package com.czy.ai.qwen.web.controller;

import com.czy.ai.qwen.common.annotation.RateLimit;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.service.IAiStreamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ai/stream")
public class StreamController {

    @Resource
    private IAiStreamService streamService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit
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