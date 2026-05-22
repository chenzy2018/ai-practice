package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Qwen AI 聊天提供者
 * 使用父类的通用实现，支持动态模型创建和缓存
 *
 * @author nober
 */
@Slf4j
@Service
public class QwenAiChatProvider extends AbstractAiChatProvider<ChatRequest> {

    @Override
    protected AiType supportAiModelImpl() {
        return AiType.QWEN;
    }

    /**
     * 如果需要特殊处理，可以重写此方法
     * 默认使用父类的实现
     */
    @Override
    public Result<String> customChat(ChatRequest request) {
        log.debug("Qwen customChat 请求: userId={}, question={}", request.getUserId(), request.getQuestion());
        return super.customChat(request);
    }
}
