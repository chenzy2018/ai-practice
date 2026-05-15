package com.czy.ai.qwen.infrastructure.client;

import com.czy.ai.qwen.common.config.QwenConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 封装服务
 * 提供统一的 HTTP 请求能力
 *
 * @author chenzhenyu 2026年05月15日
 */
@Slf4j
@Service
public class OkHttpService {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    @Resource
    private QwenConfig qwenConfig;

    private volatile OkHttpClient client;
    private volatile OkHttpClient streamClient;

    private OkHttpClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    QwenConfig.OkHttpConfig httpConfig = qwenConfig.getHttp();
                    client = new OkHttpClient.Builder()
                            .connectTimeout(httpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                            .readTimeout(httpConfig.getReadTimeout(), TimeUnit.SECONDS)
                            .writeTimeout(httpConfig.getWriteTimeout(), TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return client;
    }

    private OkHttpClient getStreamClient() {
        if (streamClient == null) {
            synchronized (this) {
                if (streamClient == null) {
                    QwenConfig.OkHttpConfig httpConfig = qwenConfig.getHttp();
                    streamClient = new OkHttpClient.Builder()
                            .connectTimeout(httpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                            .readTimeout(httpConfig.getStreamReadTimeout(), TimeUnit.SECONDS)
                            .writeTimeout(httpConfig.getWriteTimeout(), TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return streamClient;
    }

    public String post(String url, String jsonBody, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("HTTP请求失败 - status: {}, errorBody: {}", response.code(), errorBody);
                throw new IOException("HTTP请求失败: " + response.code());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    public Response postStream(String url, String jsonBody, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Accept", "text/event-stream")
                .build();

        Response response = getStreamClient().newCall(request).execute();

        if (!response.isSuccessful()) {
            String errorBody = response.body() != null ? response.body().string() : "";
            log.error("HTTP流式请求失败 - status: {}, errorBody: {}", response.code(), errorBody);
            response.close();
            throw new IOException("HTTP流式请求失败: " + response.code());
        }

        log.info("流式请求连接成功 - status: {}, content-type: {}",
                response.code(), response.header("Content-Type"));

        return response;
    }
}
