package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Messages")
public class Message {
    // 消息类型常量
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_FAVORITE = "favorite";
    public static final String TYPE_ADMIN_MESSAGE = "admin_message";

    @TableId(value = "MessageId", type = IdType.AUTO)
    private Integer messageId;
    
    @TableField("UserId")
    private Integer userId;      // 接收消息的用户
    
    @TableField("PostId")
    private Integer postId;      // 相关文章ID
    
    @TableField("CommentId")
    private Integer commentId;   // 相关评论ID
    
    @TableField("Type")
    private String type;        // 消息类型
    
    @TableField("Content")
    private String content;     // 消息内容
    
    @TableField("IsRead")
    private Boolean isRead;
    
    @TableField("CreatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField("UpdatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @TableField("SenderId")
    private Integer senderId;    // 发送者ID
    
    @TableField("SenderName")
    private String senderName;   // 发送者用户名
    
    @TableField("SenderEmail")
    private String senderEmail;  // 发送者邮箱
    
    @TableField(exist = false)
    private String postTitle;    // 相关文章标题（非数据库字段）
} 