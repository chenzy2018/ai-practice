package com.czy.ai.langchain4j;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 聊天提供者注册中心
 * 按 AiType 索引 IAiChatProvider 实例
 *
 * @author chenzhenyu 2026年05月19日 上午09:49:40
 */
@Component
@Slf4j
public class AiChatProviderFactory implements InitializingBean {

    @Autowired
    private List<IAiChatProvider> aiChatProviders;

    private final ConcurrentHashMap<AiType, IAiChatProvider> providerMap = new ConcurrentHashMap<>();

    public IAiChatProvider getAiChatProvider(AiType aiType) {
        return providerMap.get(aiType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        aiChatProviders.forEach(action -> providerMap.put(action.supportAiModel(), action));
    }
}
