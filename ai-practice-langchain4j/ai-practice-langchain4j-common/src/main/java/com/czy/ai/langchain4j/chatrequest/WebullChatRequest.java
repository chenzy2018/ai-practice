package com.czy.ai.langchain4j.chatrequest;

/**
 * @author chenzhenyu 2026年05月19日 上午10:39:59
 */
public interface WebullChatRequest extends ChatRequest {

    /**
     * 是否流式
     */
    Boolean getStream();
}
