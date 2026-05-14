package com.czy.qwen.server;

import com.czy.qwen.dto.ChatRequest;
import com.czy.qwen.resp.Result;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

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

    Result<Set<String>> listSessions();

    Result<String> removeSession(String sessionId);

    Result<Map<String, Object>> getSessionInfo(String sessionId);

    @Getter
    @Setter
    @Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class ChatResponse {
        private String sessionId;
        private String answer;
    }
}