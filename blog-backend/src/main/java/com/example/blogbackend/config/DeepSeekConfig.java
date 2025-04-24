package com.example.blogbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@Configuration
public class DeepSeekConfig {
    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
} 