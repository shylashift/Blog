package com.example.blogbackend.entity;

import javax.persistence.*;
import lombok.Data;            // Lombok：自动生成 getter/setter 等方法
import java.time.LocalDateTime;   //Java 8 引入的日期时间 API，用于处理日期和时间：

@Data
@Entity                       // JPA：标记这是一个数据库实体
@Table(name = "\"ChatMessages\"") // JPA：指定数据库表名
public class ChatMessage {
    @Id               // JPA：标记主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ChatId\"")
    private Integer chatId;

    @Column(name = "\"UserId\"", nullable = false)
    private Integer userId;

    @Column(name = "\"Role\"", nullable = false, length = 50)
    private String role;

    @Column(name = "\"Content\"", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "\"CreatedAt\"", nullable = false)  // Java 时间类型：存储消息创建时间
    private LocalDateTime createdAt;

    @PrePersist               // JPA：在保存前自动设置时间
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 