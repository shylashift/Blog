package com.example.blogbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogbackend.entity.Role;
import com.example.blogbackend.entity.UserRole;
import com.example.blogbackend.mapper.RoleMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.mapper.UserRoleMapper;
import com.example.blogbackend.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Autowired
    public AdminServiceImpl(
            UserMapper userMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    @Transactional
    public void assignAdminRole(Integer userId) {
        log.info("开始为用户 {} 分配管理员角色", userId);
        
        // 检查用户是否存在
        if (userMapper.selectById(userId) == null) {
            log.error("用户 {} 不存在", userId);
            throw new RuntimeException("用户不存在");
        }

        // 获取admin角色
        Role adminRole = roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, "ROLE_ADMIN")
        );

        if (adminRole == null) {
            log.info("管理员角色不存在，创建新的管理员角色");
            // 如果admin角色不存在，创建它
            adminRole = Role.builder()
                .roleName("ROLE_ADMIN")
                .description("管理员角色")
                .createdAt(LocalDateTime.now())
                .build();
            roleMapper.insert(adminRole);
        }

        // 检查用户是否已经有admin角色
        long count = userRoleMapper.selectCount(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, adminRole.getRoleId())
        );

        if (count == 0) {
            log.info("为用户 {} 添加管理员角色", userId);
            // 如果用户还没有admin角色，添加它
            UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(adminRole.getRoleId())
                .createdAt(LocalDateTime.now())
                .build();
            userRoleMapper.insert(userRole);
            log.info("成功为用户 {} 添加管理员角色", userId);
        } else {
            log.info("用户 {} 已经拥有管理员角色", userId);
        }
    }

    @Override
    @Transactional
    public void removeAdminRole(Integer userId) {
        log.info("开始移除用户 {} 的管理员角色", userId);
        roleMapper.removeAdminRole(userId);
        log.info("成功移除用户 {} 的管理员角色", userId);
    }

    @Override
    public boolean isAdmin(Integer userId) {
        log.info("检查用户 {} 是否为管理员", userId);
        boolean isAdmin = roleMapper.hasAdminRole(userId);
        log.info("用户 {} {}是管理员", userId, isAdmin ? "" : "不");
        return isAdmin;
    }
} 