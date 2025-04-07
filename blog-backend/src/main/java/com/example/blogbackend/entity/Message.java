package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer messageId;
    private Integer userId;      // 接收消息的用户
    private Integer postId;      // 相关文章
    private Integer commentId;   // 相关评论（如果是评论消息）
    private String type;        // 'comment' 或 'favorite'
    private Boolean isRead;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String senderName;   // 发送者用户名（非数据库字段）
    @TableField(exist = false)
    private String postTitle;    // 相关文章标题（非数据库字段）
} 