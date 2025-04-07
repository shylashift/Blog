package com.example.blogbackend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Integer id;
    private String type;  // 'comment' 或 'favorite'
    private String title;
    private String content;
    private String author;
    private Integer postId;
    private LocalDateTime createdAt;
    private boolean isRead;
} 