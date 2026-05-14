package com.czy.qwen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QwenAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QwenAiApplication.class, args);
    }
}