package com.czy.qwen.server;

import com.czy.qwen.facade.Message;
import com.czy.qwen.facade.QwenApiClient;
import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.util.AiParamUtils;
import com.czy.qwen.util.AiStringUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Qwen AI 流式服务实现
 */
@Slf4j
@Service
public class QwenStreamServiceImpl implements IQwenStreamService {

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @Resource
    private SessionManager sessionManager;

    @Resource
    private QwenApiClient qwenApiClient;

    @Override
    public SseEmitter chatStreamWithContext(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AiParamUtils.EffectiveParams params = AiParamUtils.extractParams(request);

        executorService.execute(() -> {
            try {
                SessionContext sessionContext = sessionManager.getOrCreate(params.getSessionId(), params.getSystemPrompt());
                sessionContext.addUserMessage(AiStringUtils.escapeJson(params.getQuestion()));

                String response = sendStreamRequest(new ArrayList<>(sessionContext.getMessages()),
                        params.getModel(), params.getTemperature(), emitter);

                sessionContext.addAssistantMessage(response);
                sessionManager.updateLastActiveTime(params.getSessionId());
                emitter.complete();
            } catch (Exception e) {
                log.error("流式对话异常 - sessionId: {}, error: {}", params.getSessionId(), e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String sendStreamRequest(java.util.List<Message> messages, String model, Double temperature,
                                     SseEmitter emitter) throws IOException {
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
                    if (line.isEmpty()) continue;

                    String content = parseStreamLine(line);
                    if (content != null && !content.isEmpty()) {
                        fullResponse.setLength(0);
                        fullResponse.append(content);

                        int currentLength = content.length();
                        if (currentLength > lastLength) {
                            emitter.send(content.substring(lastLength));
                            lastLength = currentLength;
                        }
                    }
                }
            }

            emitter.send("[FINISHED]");
        }

        return fullResponse.toString();
    }

    private String parseStreamLine(String line) {
        if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            if (data.length() < 10) {
                return null;
            }

            try {
                int textIdx = data.indexOf("\"text\":\"");
                if (textIdx > 0) {
                    int end = data.indexOf("\"", textIdx + 8);
                    if (end > textIdx) {
                        return data.substring(textIdx + 8, end);
                    }
                }
            } catch (Exception e) {
                log.error("解析失败：{}", e.getMessage());
            }
        }
        return null;
    }
}