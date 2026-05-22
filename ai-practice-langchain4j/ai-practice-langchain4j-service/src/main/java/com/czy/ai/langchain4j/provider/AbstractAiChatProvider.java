package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.Message;
import com.czy.ai.common.dto.Result;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.common.session.SessionManage;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.IAiChatProvider;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * LangChain4j 抽象类 - 使用动态模型工厂优化
 *
 * @author chenzhenyu 2026年05月18日 下午22:04:20
 */
@Slf4j
public abstract class AbstractAiChatProvider<T extends ChatRequest> implements IAiChatProvider<T> {

    @Autowired
    private ChatLanguageModelFactory aiChatProviderFactory;

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
        String answer = aiChatProviderFactory.getAiChatProvider(supportAiModel()).generate(content);
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
        ChatLanguageModel model = dynamicModelFactory.createChatModel(chatRequest, supportAiModel());

        // 3. 构建消息列表 - 将内部 Message 转换为 LangChain4j 的 ChatMessage
        List<ChatMessage> messages = convertMessages(session.getMessages());

        // 如果有系统提示，添加到消息列表开头
        if (chatRequest.getSystemPrompt() != null && !chatRequest.getSystemPrompt().isEmpty()) {
            messages.add(0, SystemMessage.from(chatRequest.getSystemPrompt()));
        }

        // 添加用户消息
        messages.add(UserMessage.from(chatRequest.getQuestion()));

        // 4. 调用模型生成响应
        Response<AiMessage> response = model.generate(messages);

        // 5. 更新会话
        String content = response.content().text();
        session.addAssistantMessage(content);

        log.debug("AI响应: {}", content);
        return Result.success(content);
    }

    /**
     * 将内部 Message 列表转换为 LangChain4j 的 ChatMessage 列表
     */
    private List<ChatMessage> convertMessages(List<Message> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (Message message : messages) {
            switch (message.getRole()) {
                case Message.ROLE_USER:
                    chatMessages.add(UserMessage.from(message.getContent()));
                    break;
                case Message.ROLE_ASSISTANT:
                    chatMessages.add(AiMessage.from(message.getContent()));
                    break;
                case Message.ROLE_SYSTEM:
                    chatMessages.add(SystemMessage.from(message.getContent()));
                    break;
                default:
                    chatMessages.add(UserMessage.from(message.getContent()));
            }
        }
        return chatMessages;
    }

    /**
     * 获取动态模型工厂（供子类使用）
     */
    protected DynamicModelFactory getDynamicModelFactory() {
        return dynamicModelFactory;
    }
}
