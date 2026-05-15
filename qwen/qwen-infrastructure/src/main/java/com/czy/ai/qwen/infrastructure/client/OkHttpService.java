package com.czy.ai.qwen.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

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

    private final OkHttpClient client;
    private final OkHttpClient streamClient;

    public OkHttpService() {
        this.client = createOkHttpClient();
        this.streamClient = createStreamOkHttpClient();
    }

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    private OkHttpClient createStreamOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String post(String url, String jsonBody, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
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

        Response response = streamClient.newCall(request).execute();

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

    public OkHttpClient getClient() {
        return client;
    }
}