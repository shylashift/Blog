package com.example.blogbackend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Integer id;
    private Integer postId;
    private String title;
    private String author;
    private LocalDateTime createdAt;
} 