package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatModelFactory;
import com.czy.ai.langchain4j.IAiChatProvider;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.service.AiAssistant;
import com.czy.ai.langchain4j.service.ChatMemoryManager;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * LangChain4j 抽象类 - 高阶 AiServices + 低阶 ChatMemory 混合模式
 *
 * 高阶路径（chat）：AiAssistant（AiServices 自动编排记忆 + 模型调用）
 * 低阶路径（customChat）：ChatMemoryManager + DynamicModelFactory（动态模型参数）
 *
 * @author chenzhenyu 2026年05月18日 下午22:04:20
 */
@Slf4j
public abstract class AbstractAiChatProvider<T extends ChatRequest> implements IAiChatProvider<T> {

    @Autowired
    private ChatModelFactory chatModelFactory;

    @Autowired
    private DynamicModelFactory dynamicModelFactory;

    @Autowired
    protected ChatMemoryManager chatMemoryManager;

    @Autowired
    private Map<AiType, AiAssistant> aiAssistantMap;

    /**
     * 获取支持的 AI 类型
     */
    @Override
    public AiType supportAiModel() {
        return supportAiModelImpl();
    }

    /**
     * 子类实现：返回支持的 AI 类型
     */
    protected abstract AiType supportAiModelImpl();

    /**
     * 简单对话 - 使用 AiAssistant 高阶 API
     * AiServices 自动编排：UserMessage → ChatModel → AiMessage → ChatMemory
     */
    @Override
    public Result<String> chat(String content) {
        AiAssistant assistant = aiAssistantMap.get(supportAiModel());
        if (assistant != null) {
            // 使用默认 sessionId = "default"，无记忆
            String answer = assistant.chat("default", content);
            return Result.success(answer);
        }
        // 降级：直接使用 ChatModel
        String answer = chatModelFactory.getChatModel(supportAiModel()).chat(content);
        return Result.success(answer);
    }

    /**
     * 自定义对话 - 低阶 API（动态模型参数 + ChatMemory）
     * 需要动态模型参数，无法使用 AiServices 的固定模型绑定
     */
    @Override
    public Result<String> customChat(T chatRequest) {
        // 1. 获取/创建 ChatMemory（自动处理 SystemMessage + 消息淘汰）
        ChatMemory chatMemory = chatMemoryManager.getOrCreate(
                chatRequest.getSessionId(),
                chatRequest.getUserId(),
                chatRequest.getSystemPrompt());

        // 2. 添加用户消息到 ChatMemory
        chatMemory.add(UserMessage.from(chatRequest.getQuestion()));

        // 3. 动态创建模型（带缓存）
        ChatModel model = dynamicModelFactory.createChatModel(chatRequest, supportAiModel());

        // 4. 使用 ChatMemory 中的所有消息调用模型
        ChatResponse response = model.chat(chatMemory.messages());

        // 5. 将 AI 响应加入 ChatMemory
        String content = response.aiMessage().text();
        chatMemory.add(response.aiMessage());

        log.debug("AI响应: aiType={}, userId={}", supportAiModel(), chatRequest.getUserId());
        return Result.success(content);
    }

    /**
     * 获取动态模型工厂（供子类使用）
     */
    protected DynamicModelFactory getDynamicModelFactory() {
        return dynamicModelFactory;
    }
}
