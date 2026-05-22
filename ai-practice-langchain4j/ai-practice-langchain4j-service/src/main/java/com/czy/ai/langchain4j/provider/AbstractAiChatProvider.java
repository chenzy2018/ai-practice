package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.dto.Result;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.common.session.SessionManage;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatModelFactory;
import com.czy.ai.langchain4j.IAiChatProvider;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.util.MessageConverter;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * LangChain4j 抽象类 - 使用动态模型工厂优化
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
    protected SessionManage sessionManage;

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
     * 简单对话 - 使用已注册的模型
     */
    @Override
    public Result<String> chat(String content) {
        String answer = chatModelFactory.getChatModel(supportAiModel()).chat(content);
        return Result.success(answer);
    }

    /**
     * 自定义对话 - 使用动态创建的模型
     */
    @Override
    public Result<String> customChat(T chatRequest) {
        // 1. 获取会话上下文
        SessionContext session = sessionManage.getOrCreate(
                chatRequest.getSessionId(),
                chatRequest.getUserId(),
                chatRequest.getSystemPrompt()
        );
        sessionManage.updateLastActiveTime(chatRequest.getSessionId());

        // 2. 动态创建模型（带缓存）
        ChatModel model = dynamicModelFactory.createChatModel(chatRequest, supportAiModel());

        // 3. 使用 MessageConverter 构建消息列表
        List<ChatMessage> messages = MessageConverter.buildMessageList(
                session.getMessages(), chatRequest.getQuestion(), chatRequest.getSystemPrompt());

        // 4. 调用模型生成响应
        ChatResponse response = model.chat(messages);

        // 5. 更新会话
        String content = response.aiMessage().text();
        session.addAssistantMessage(content);

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
