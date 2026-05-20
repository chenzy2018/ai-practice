package com.czy.ai.langchain4j;

import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;

/**
 * longchain4j 接口
 *
 * @author chenzhenyu 2026年05月18日 下午21:22:50
 */
public interface IAiChatProvider<T extends ChatRequest> {

    /**
     * 支持的ai模型
     * @return
     */
    AiType supportAiModel();

    /**
     * 聊天
     * @param content 内容
     * @return 结果
     */
    Result<String> chat(String content);

    /**
     * 自定义聊天
     * @param chatRequest 自定义ai参数
     * @return 结果
     */
    Result<String> customChat(T chatRequest);
}
