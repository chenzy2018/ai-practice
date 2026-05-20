package com.czy.ai.langchain4j;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author chenzhenyu 2026年05月19日 上午09:49:40
 */
@Component
@Slf4j
public class AiChatProviderFactory implements InitializingBean {

    @Autowired
    private List<IAiChatProvider> aiChatProviders;

    private static final Map<AiType, IAiChatProvider> AI_CHAT_PROVIDER_MAP = Maps.newHashMap();

    public IAiChatProvider getAiChatProvider(AiType aiType) {
        return AI_CHAT_PROVIDER_MAP.get(aiType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        aiChatProviders.forEach(action -> AI_CHAT_PROVIDER_MAP.put(action.supportAiModel(), action));
    }
}
