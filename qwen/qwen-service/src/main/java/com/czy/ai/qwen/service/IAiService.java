package com.czy.ai.qwen.service;

import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.dto.ChatResponse;
import com.czy.ai.qwen.common.dto.Result;
import com.czy.ai.qwen.common.dto.SessionInfo;

import java.util.List;
import java.util.Map;

public interface IAiService {

    String generateSessionId();

    Result<String> chat(String question, String model, Double temperature);

    Result<ChatResponse> chatWithContext(ChatRequest request);

    Result<String> clearContext(String sessionId);

    Result<Map<String, List<SessionInfo>>> listSessions();

    Result<String> removeSession(String sessionId);

    Result<Map<String, Object>> getSessionInfo(String sessionId);
}