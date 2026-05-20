package com.czy.ai.langchain4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {
                "com.czy.ai.langchain4j",
                "com.czy.ai.common",
                "com.czy.ai.web"
        })
@EnableConfigurationProperties
public class LangChainAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangChainAiApplication.class, args);
    }
}