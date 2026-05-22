package com.czy.ai.langchain4j.provider;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.StreamableChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Webull AI 聊天提供者
 * 使用 StreamableChatRequest 接口，约束 Webull 需要“流式”参数
 *
 * @author nober
 */
@Slf4j
@Service
public class WebullAiChatProvider extends AbstractAiChatProvider<StreamableChatRequest> {

    @Override
    protected AiType supportAiModelImpl() {
        return AiType.WEBULL;
    }
}
