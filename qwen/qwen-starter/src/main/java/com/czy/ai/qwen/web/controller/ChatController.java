package com.czy.ai.qwen.web.controller;

import com.czy.ai.qwen.common.annotation.RateLimit;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.dto.ChatResponse;
import com.czy.ai.qwen.common.dto.Result;
import com.czy.ai.qwen.common.dto.SessionInfo;
import com.czy.ai.qwen.service.IAiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Resource
    private IAiService aiService;

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String question,
                               @RequestParam(required = false) String model,
                               @RequestParam(required = false) Double temperature) {
        return aiService.chat(question, model, temperature);
    }

    @PostMapping("/chatWithContext")
    @RateLimit
    public Result<ChatResponse> chatWithContext(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = httpRequest.getRemoteAddr();
        }
        request.setUserId(userId);
        return aiService.chatWithContext(request);
    }

    @GetMapping("/clear")
    public Result<String> clear(@RequestParam(required = false) String sessionId) {
        return aiService.clearContext(sessionId);
    }

    @GetMapping("/session/new")
    @RateLimit
    public Result<String> createSession(HttpServletRequest request) {
        return Result.success(aiService.generateSessionId());
    }

    @GetMapping("/session/list")
    public Result<Map<String, List<SessionInfo>>> listSessions() {
        return aiService.listSessions();
    }

    @GetMapping("/session/info")
    public Result<Map<String, Object>> getSessionInfo(@RequestParam String sessionId) {
        return aiService.getSessionInfo(sessionId);
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<String> removeSession(@PathVariable String sessionId) {
        return aiService.removeSession(sessionId);
    }
}