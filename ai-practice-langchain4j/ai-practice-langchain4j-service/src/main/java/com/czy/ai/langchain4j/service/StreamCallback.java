package com.czy.ai.langchain4j.service;

import java.util.Map;

/**
 * 流式对话回调接口
 * 解耦 Service 层与 Spring Web（SseEmitter）的依赖
 *
 * @author chenzhenyu
 */
public interface StreamCallback {

    /**
     * 收到一个 token 片段
     *
     * @param token token 内容
     */
    void onToken(String token);

    /**
     * 流式生成完成
     *
     * @param fullContent 完整响应内容
     * @param metadata    元数据（sessionId, userId, aiType, costTime 等）
     */
    void onComplete(String fullContent, Map<String, Object> metadata);

    /**
     * 发生错误
     *
     * @param error 异常
     */
    void onError(Throwable error);
}
