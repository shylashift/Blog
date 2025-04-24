package com.example.blogbackend.service;

import com.example.blogbackend.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {
    private final RestTemplate restTemplate;
    private final DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeepSeekService(RestTemplate restTemplate, DeepSeekConfig deepSeekConfig) {
        this.restTemplate = restTemplate;
        this.deepSeekConfig = deepSeekConfig;
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            // 创建消息数组
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "你是一个专业的写作助手，擅长帮助用户改进文章质量，提供写作建议，并回答与写作相关的问题。"
            ));
            messages.add(Map.of(
                "role", "user",
                "content", prompt
            ));
            requestBody.set("messages", objectMapper.valueToTree(messages));

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            String url = deepSeekConfig.getApiUrl() + "/chat/completions";

            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);
            if (response != null && response.has("choices") && response.get("choices").size() > 0) {
                return response.get("choices").get(0).get("message").get("content").asText();
            }
            return "抱歉，我现在无法生成回复。";
        } catch (Exception e) {
            e.printStackTrace();
            return "生成回复时发生错误，请稍后再试。";
        }
    }
} 