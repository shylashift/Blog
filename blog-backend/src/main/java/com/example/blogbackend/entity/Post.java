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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Posts")
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField("UpdatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private User author;

    @TableField("Tags")
    private String tags;

    @TableField("AuthorName")
    private String authorName;

    @TableField("AuthorAvatar")
    private String authorAvatar;

    @TableField("IsHidden")
    private Boolean isHidden;

    @TableField(exist = false)
    private List<Comment> comments;
    
    @TableField(exist = false)
    private Integer commentCount;

    public List<String> getTagsList() {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(tags.split(","));
    }

    public void setTagsList(List<String> tagsList) {
        if (tagsList == null || tagsList.isEmpty()) {
            this.tags = "";
        } else {
            this.tags = String.join(",", tagsList);
        }
    }
} 