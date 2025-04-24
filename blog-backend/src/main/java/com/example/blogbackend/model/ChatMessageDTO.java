package com.example.blogbackend.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private String id;
    private Long chatId;      // 会话ID
    private Long userId;      // 用户ID
    private String role;      // user 或 assistant
    private String content;   // 消息内容
    private LocalDateTime createdAt;
} 