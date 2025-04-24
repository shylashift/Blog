package com.example.blogbackend.model;

import lombok.Data;

@Data
public class ChatRequest {
    private Long chatId;    // 会话ID（可选，用于关联多轮对话）
    private String content; // 消息内容
} 