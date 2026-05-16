package com.czy.ai.qwen.web.config;

import com.czy.ai.qwen.common.config.QwenConfig;
import com.czy.ai.web.client.OkHttpService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class QwenWebConfig {

    @Resource
    private QwenConfig qwenConfig;

    @Bean
    public OkHttpService okHttpService() {
        return new OkHttpService(
                () -> new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(qwenConfig.getHttp().getConnectTimeout(), TimeUnit.SECONDS)
                        .readTimeout(qwenConfig.getHttp().getReadTimeout(), TimeUnit.SECONDS)
                        .writeTimeout(qwenConfig.getHttp().getWriteTimeout(), TimeUnit.SECONDS)
                        .build(),
                () -> new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(qwenConfig.getHttp().getConnectTimeout(), TimeUnit.SECONDS)
                        .readTimeout(qwenConfig.getHttp().getStreamReadTimeout(), TimeUnit.SECONDS)
                        .writeTimeout(qwenConfig.getHttp().getWriteTimeout(), TimeUnit.SECONDS)
                        .build()
        );
    }
}
