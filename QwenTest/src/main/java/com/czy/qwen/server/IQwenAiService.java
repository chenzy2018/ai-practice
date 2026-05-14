package com.czy.qwen.server;

import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.resp.ChatResponse;
import com.czy.qwen.resp.Result;
import com.czy.qwen.resp.SessionInfo;

import java.util.List;
import java.util.Map;

/**
 * Qwen AI 服务接口
 *
 * @author chenzhenyu 2026年05月14日
 */
public interface IQwenAiService {

    String DEFAULT_MODEL = "qwen-turbo";
    Double DEFAULT_TEMPERATURE = 0.7;

    String generateSessionId();

    Result<String> chat(String question, String model, Double temperature);

    Result<ChatResponse> chatWithContext(ChatRequest request);

    Result<String> clearContext(String sessionId);

    Result<List<SessionInfo>> listSessions();

    Result<String> removeSession(String sessionId);

    Result<Map<String, Object>> getSessionInfo(String sessionId);
}