package com.czy.ai.langchain4j.configration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author chenzhenyu 2026年05月20日 下午16:43:41
 */
@Configuration
public class RestTemplateConfigration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
