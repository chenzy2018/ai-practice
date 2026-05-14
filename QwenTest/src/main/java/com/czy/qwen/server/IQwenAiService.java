package com.czy.qwen.server;

import com.czy.qwen.dto.ChatRequest;
import com.czy.qwen.resp.Result;

import java.util.Set;

/**
 * Qwen AI 服务接口
 *
 * @author chenzhenyu 2026年05月14日
 */
public interface IQwenAiService {

    String DEFAULT_MODEL = "qwen-turbo";
    Double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 生成新的会话ID
     *
     * @return 会话ID
     */
    String generateSessionId();

    /**
     * 单轮对话
     *
     * @param question    用户问题
     * @param model       模型名称，默认 "qwen-turbo"
     * @param temperature 温度，控制随机性/创意，范围 0~1，默认 0.7
     * @return 回答结果
     */
    Result<String> chat(String question, String model, Double temperature);

    /**
     * 多轮对话（带上下文）
     *
     * @param request 聊天请求参数，包含 sessionId, question, systemPrompt, model, temperature
     *                - sessionId: 会话ID，为空则生成新会话
     *                - question: 用户问题
     *                - systemPrompt: 系统角色Prompt，可选，不传则不带system消息
     *                - model: 模型名称，默认 "qwen-turbo"
     *                - temperature: 温度，默认 0.7
     * @return 回答结果（包含sessionId）
     */
    Result<ChatResponse> chatWithContext(ChatRequest request);

    /**
     * 清空指定会话的上下文
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    Result<String> clearContext(String sessionId);

    /**
     * 获取所有活跃会话ID
     *
     * @return 会话ID列表
     */
    Result<Set<String>> listSessions();

    /**
     * 删除指定会话
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    Result<String> removeSession(String sessionId);

    /**
     * 聊天响应（包含会话ID）
     */
    class ChatResponse {
        private String sessionId;
        private String answer;

        public ChatResponse() {
        }

        public ChatResponse(String sessionId, String answer) {
            this.sessionId = sessionId;
            this.answer = answer;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}