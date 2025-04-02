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
@TableName("Messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer messageId;
    private Integer userId;
    private Integer postId;
    private Integer commentId;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private Post post;
    @TableField(exist = false)
    private Comment comment;
} 