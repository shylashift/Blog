package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Posts")
public class Post {
    @TableId(value = "PostId", type = IdType.AUTO)
    private Integer postId;
    
    @TableField("UserId")
    private Integer userId;
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在1-200之间")
    @TableField("Title")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    @TableField("Content")
    private String content;
    
    @Size(max = 500, message = "摘要长度不能超过500")
    @TableField("Summary")
    private String summary;
    
    @TableField("CreatedAt")
    private LocalDateTime createdAt;
    
    @TableField("UpdatedAt")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private User user;
} 