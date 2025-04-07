package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("UserRoles")
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Integer userRoleId;
    private Integer userId;
    private Integer roleId;
    private LocalDateTime createdAt;
} 