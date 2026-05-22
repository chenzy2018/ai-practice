package com.czy.ai.langchain4j.chatrequest;

/**
 * 支持流式响应的聊天请求接口
 * 扩展 ChatRequest，增加流式控制能力
 * 需要流式响应的 AI 提供商（如 Webull）通过此接口约定参数
 *
 * @author chenzhenyu 2026年05月19日 上午10:39:59
 */
public interface StreamableChatRequest extends ChatRequest {

    /**
     * 是否流式
     */
    Boolean getStream();
}
