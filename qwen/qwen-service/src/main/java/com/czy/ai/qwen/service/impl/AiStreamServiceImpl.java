package com.czy.ai.qwen.service.impl;

import com.czy.ai.qwen.common.Message;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.util.AiParamUtils;
import com.czy.ai.qwen.common.util.AiStringUtils;
import com.czy.ai.qwen.common.session.SessionContext;
import com.czy.ai.qwen.common.outclient.QwenApiClient;
import com.czy.ai.qwen.common.session.SessionManage;
import com.czy.ai.qwen.service.IAiStreamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 流式对话服务实现
 * 提供基于 SSE 的流式响应能力
 *
 * @author nober 2026年05月15日
 */
@Slf4j
@Service
public class AiStreamServiceImpl implements IAiStreamService {

    private static final long SSE_TIMEOUT = 300000L;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private SessionManage sessionManage;

    @Resource
    private QwenApiClient qwenApiClient;

    /**
     * 流式对话（带上下文）
     *
     * @param request 对话请求
     * @return SseEmitter 用于流式推送
     */
    @Override
    public SseEmitter chatStreamWithContext(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AiParamUtils.EffectiveParams params = AiParamUtils.extractParams(request);
        String sessionId = params.getSessionId();
        String userId = params.getUserId();

        new Thread(() -> {
            try {
                SessionContext session = sessionManage.getOrCreate(sessionId, userId, params.getSystemPrompt());
                session.addUserMessage(AiStringUtils.escapeJson(params.getQuestion()));

                log.info("[流式对话 入参] sessionId: {}, userId: {}, question: {}",
                        sessionId, userId, AiStringUtils.escapeJson(params.getQuestion()));

                String fullResponse = sendStreamRequest(new ArrayList<>(session.getMessages()), params.getModel(), params.getTemperature(), emitter);
                session.addAssistantMessage(fullResponse);
                sessionManage.updateLastActiveTime(sessionId);

                Map<String, Object> finishedMsg = new HashMap<>();
                finishedMsg.put("sessionId", sessionId);
                finishedMsg.put("userId", userId);
                finishedMsg.put("content", "[FINISHED]");
                emitter.send(objectMapper.writeValueAsString(finishedMsg));
                emitter.complete();

                log.info("[流式对话 出参] sessionId: {}, answer: {}", sessionId, fullResponse);
            } catch (Exception e) {
                log.error("流式对话异常 - sessionId: {}, error: {}", sessionId, e.getMessage());
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 发送流式请求并处理响应
     *
     * @param messages 消息列表
     * @param model    模型名称
     * @param temperature 温度参数
     * @param emitter  SseEmitter
     * @return 完整响应内容
     * @throws IOException IO异常
     */
    private String sendStreamRequest(java.util.List<Message> messages, String model, Double temperature, SseEmitter emitter) throws IOException {
        StringBuilder fullResponse = new StringBuilder();
        int lastLength = 0;

        try (Response response = qwenApiClient.sendStreamRequest(messages, model, temperature)) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("响应体为空");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }

                    String content = parseStreamLine(line);
                    if (content != null && !content.isEmpty()) {
                        fullResponse.setLength(0);
                        fullResponse.append(content);

                        int currentLength = content.length();
                        if (currentLength > lastLength) {
                            String chunk = content.substring(lastLength);
                            emitter.send(chunk);
                            lastLength = currentLength;
                        }
                    }
                }
            }
        }

        return fullResponse.toString();
    }

    /**
     * 解析 SSE 流式响应行
     *
     * @param line 响应行
     * @return 解析后的文本内容
     */
    private String parseStreamLine(String line) {
        if (!line.startsWith("data:")) {
            return null;
        }

        String data = line.substring(5).trim();
        if (data.isEmpty()) {
            return null;
        }

        int textStart = data.indexOf("\"text\":\"");
        if (textStart == -1) {
            textStart = data.indexOf("\"content\":\"");
        }

        if (textStart == -1) {
            return null;
        }

        int start = textStart + 8;
        int end = data.indexOf("\"", start);
        if (end == -1) {
            return null;
        }

        return data.substring(start, end);
    }
}
