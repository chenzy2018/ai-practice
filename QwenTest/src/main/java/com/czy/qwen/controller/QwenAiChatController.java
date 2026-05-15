package com.czy.qwen.controller;

import com.czy.qwen.annotation.RateLimit;
import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.resp.ChatResponse;
import com.czy.qwen.resp.Result;
import com.czy.qwen.resp.SessionInfo;
import com.czy.qwen.server.IQwenAiService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Qwen AI 聊天控制器
 *
 * @author chenzhenyu 2026年05月14日
 */
@RestController
public class QwenAiChatController {

    @Resource
    private IQwenAiService qwenAiService;

    @GetMapping("/ai/chat")
    public Result<String> chat(@RequestParam String question,
                               @RequestParam(required = false) String model,
                               @RequestParam(required = false) Double temperature) {
        return qwenAiService.chat(question, model, temperature);
    }

    @PostMapping("/ai/chatWithContext")
    @RateLimit
    public Result<ChatResponse> chatWithContext(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = httpRequest.getRemoteAddr();
        }
        request.setUserId(userId);
        return qwenAiService.chatWithContext(request);
    }

    @GetMapping("/ai/clear")
    public Result<String> clear(@RequestParam(required = false) String sessionId) {
        return qwenAiService.clearContext(sessionId);
    }

    @GetMapping("/ai/session/new")
    @RateLimit
    public Result<String> createSession(HttpServletRequest request) {
        return Result.success(qwenAiService.generateSessionId());
    }

    @GetMapping("/ai/session/list")
    public Result<Map<String, List<SessionInfo>>> listSessions() {
        return qwenAiService.listSessions();
    }

    @GetMapping("/ai/session/info")
    public Result<Map<String, Object>> getSessionInfo(@RequestParam String sessionId) {
        return qwenAiService.getSessionInfo(sessionId);
    }

    @DeleteMapping("/ai/session/{sessionId}")
    public Result<String> removeSession(@PathVariable String sessionId) {
        return qwenAiService.removeSession(sessionId);
    }
}