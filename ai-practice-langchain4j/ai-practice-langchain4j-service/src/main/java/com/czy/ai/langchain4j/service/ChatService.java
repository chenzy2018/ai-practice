package com.czy.ai.langchain4j.service;

import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.chatrequest.LangChainChatRequest;

/**
 * 聊天服务接口
 * 封装 AI 对话的核心业务逻辑，供 Controller 调用
 *
 * @author chenzhenyu
 */
public interface ChatService {

    /**
     * 简单对话
     *
     * @param question 用户问题
     * @param aiType   AI 类型字符串
     * @return AI 回复
     */
    Result<String> chat(String question, String aiType);

    /**
     * 自定义对话（非流式）
     *
     * @param chatRequest 自定义聊天请求
     * @return AI 回复
     */
    Result<String> customChat(LangChainChatRequest chatRequest);

    /**
     * 流式对话
     *
     * @param question     用户问题
     * @param aiType       AI 类型字符串
     * @param sessionId    会话ID（可选）
     * @param userId       用户ID（可选）
     * @param systemPrompt 系统提示（可选）
     * @param callback     流式回调
     */
    void streamChat(String question, String aiType, String sessionId, String userId,
                     String systemPrompt, StreamCallback callback);

    /**
     * 自定义流式对话
     *
     * @param chatRequest 自定义聊天请求
     * @param callback    流式回调
     */
    void streamCustomChat(LangChainChatRequest chatRequest, StreamCallback callback);
}
