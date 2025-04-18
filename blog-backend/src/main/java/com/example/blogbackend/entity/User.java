package com.example.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("Users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements UserDetails {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String username;
    @JsonIgnore
    @TableField("Password")
    private String password;
    private String email;
    private String avatar;
    private String bio;
    
    @TableField("CreatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField("UpdatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("Disabled")
    private Boolean disabled;

    @TableField(exist = false)
    private List<Role> roles;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            // 如果是admin用户，即使roles为空也给予管理员权限
            if ("admin@example.com".equals(email)) {
                return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            return List.of(new SimpleGrantedAuthority("ROLE_USER")); // 默认角色
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return disabled == null || !disabled;
    }
} 