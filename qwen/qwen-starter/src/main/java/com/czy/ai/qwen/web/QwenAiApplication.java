package com.czy.ai.qwen.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.czy.ai.qwen")
@EnableScheduling
@EnableConfigurationProperties
public class QwenAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QwenAiApplication.class, args);
    }
}