package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Comments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {
    @TableId(value = "CommentId", type = IdType.AUTO)
    private Integer commentId;
    
    @TableField("PostId")
    private Integer postId;
    
    @TableField("UserId")
    private Integer userId;
    
    @TableField("Content")
    private String content;
    
    @TableField("CreatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField("UpdatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private Post post;
} 