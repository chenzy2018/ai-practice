package com.czy.ai.langchain4j.chatrequest;

import com.czy.ai.langchain4j.AiType;

/**
 * @author chenzhenyu 2026年05月19日 上午10:39:59
 */
public interface ChatRequest {

    String getSessionId();

    String getUserId();

    String getSystemPrompt();

    /**
     * 用户问题
     */
    String getQuestion();

    /**
     * ai 类型
     * @see AiType
     */
    String getAiType();

    /**
     * 模型
     */
    String getModel();

    /**
     * 温度，控制随机性/创意
     */
    Double getTemperature();

    /**
     * 控制多样性，控制 AI 词汇选择范围，默认 0.8，一般不懂
     */
    Double getTopP();

    /**
     * 控制回答长度
     */
    Integer getMaxTokens();
}
