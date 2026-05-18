package com.czy.ai.qwen.web.controller;

import com.czy.ai.common.annotation.ControllerLog;
import com.czy.ai.common.annotation.RateLimit;
import com.czy.ai.common.dto.Result;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.dto.ChatResponse;
import com.czy.ai.qwen.common.dto.SessionInfo;
import com.czy.ai.qwen.service.IAiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * AI 对话 Controller
 * 提供 RESTful 风格的对话接口
 *
 * @author nober 2026年05月15日
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Resource
    private IAiService aiService;

    /**
     * 简单对话（无上下文）
     *
     * @param question    问题
     * @param model       模型名称（可选）
     * @param temperature 温度参数（可选）
     * @return AI 回复
     */
    @GetMapping("/chat")
    @ControllerLog(desc = "简单对话")
    public Result<String> chat(@RequestParam String question,
                               @RequestParam(required = false) String model,
                               @RequestParam(required = false) Double temperature) {
        return aiService.chat(question, model, temperature);
    }

    /**
     * 带上下文的对话
     *
     * @param request     对话请求（包含 sessionId 和 prompt）
     * @param httpRequest HTTP请求（用于获取 userId）
     * @return 对话响应
     */
    @PostMapping("/chatWithContext")
    @RateLimit
    @ControllerLog(desc = "带上下文对话")
    public Result<ChatResponse> chatWithContext(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = httpRequest.getRemoteAddr();
        }
        request.setUserId(userId);
        return aiService.chatWithContext(request);
    }

    /**
     * 清除会话上下文
     *
     * @param sessionId 会话ID（可选，为空则清除所有）
     * @return 操作结果
     */
    @GetMapping("/clear")
    @ControllerLog(desc = "清除会话上下文")
    public Result<String> clear(@RequestParam(required = false) String sessionId) {
        return aiService.clearContext(sessionId);
    }

    /**
     * 创建新会话
     *
     * @param request HTTP请求
     * @return 新会话ID
     */
    @GetMapping("/session/new")
    @RateLimit
    @ControllerLog(desc = "创建新会话")
    public Result<String> createSession(HttpServletRequest request) {
        return Result.success(aiService.generateSessionId());
    }

    /**
     * 获取会话列表
     *
     * @return 所有会话信息
     */
    @GetMapping("/session/list")
    @ControllerLog(desc = "获取会话列表")
    public Result<Map<String, List<SessionInfo>>> listSessions() {
        return aiService.listSessions();
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @GetMapping("/session/info")
    @ControllerLog(desc = "获取会话详情")
    public Result<Map<String, Object>> getSessionInfo(@RequestParam String sessionId) {
        return aiService.getSessionInfo(sessionId);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{sessionId}")
    @ControllerLog(desc = "删除会话")
    public Result<String> removeSession(@PathVariable String sessionId) {
        return aiService.removeSession(sessionId);
    }
}
