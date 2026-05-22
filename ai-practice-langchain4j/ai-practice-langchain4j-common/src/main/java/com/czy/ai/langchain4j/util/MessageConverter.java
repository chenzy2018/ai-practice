package com.czy.ai.langchain4j.util;

import com.czy.ai.common.Message;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息转换工具类
 * 统一处理内部 Message 与 LangChain4j ChatMessage 的转换
 *
 * @author chenzhenyu
 */
public class MessageConverter {

    private MessageConverter() {
    }

    /**
     * 将内部 Message 列表转换为 LangChain4j 的 ChatMessage 列表
     *
     * @param messages 内部消息列表
     * @return LangChain4j ChatMessage 列表
     */
    public static List<ChatMessage> convertToChatMessages(List<Message> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>(messages.size());
        for (Message message : messages) {
            chatMessages.add(convertSingleMessage(message));
        }
        return chatMessages;
    }

    /**
     * 构建完整的消息列表（包含历史上下文 + 当前问题）
     *
     * @param historyMessages 历史消息列表
     * @param question        当前问题
     * @param systemPrompt    系统提示
     * @return LangChain4j ChatMessage 列表
     */
    public static List<ChatMessage> buildMessageList(List<Message> historyMessages, String question, String systemPrompt) {
        List<ChatMessage> chatMessages = new ArrayList<>();

        // 1. 添加系统提示（如果有）
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            chatMessages.add(SystemMessage.from(systemPrompt));
        }

        // 2. 添加历史消息
        if (historyMessages != null && !historyMessages.isEmpty()) {
            for (Message message : historyMessages) {
                chatMessages.add(convertSingleMessage(message));
            }
        }

        // 3. 添加当前问题
        chatMessages.add(UserMessage.from(question));

        return chatMessages;
    }

    private static ChatMessage convertSingleMessage(Message message) {
        return switch (message.getRole()) {
            case Message.ROLE_USER -> UserMessage.from(message.getContent());
            case Message.ROLE_ASSISTANT -> AiMessage.from(message.getContent());
            case Message.ROLE_SYSTEM -> SystemMessage.from(message.getContent());
            default -> UserMessage.from(message.getContent());
        };
    }
}
