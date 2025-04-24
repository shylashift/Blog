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

        // 获取admin角色ID（应该是1）
        Role adminRole = roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, "ROLE_ADMIN")
        );

        if (adminRole == null) {
            log.error("管理员角色不存在");
            throw new RuntimeException("管理员角色不存在");
        }

        // 更新用户的角色
        UserRole userRole = userRoleMapper.selectOne(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        if (userRole == null) {
            // 如果用户没有角色记录，创建一个
            userRole = UserRole.builder()
                .userId(userId)
                .roleId(adminRole.getRoleId())
                .createdAt(LocalDateTime.now())
                .build();
            userRoleMapper.insert(userRole);
        } else {
            // 更新现有角色为管理员
            userRole.setRoleId(adminRole.getRoleId());
            userRoleMapper.updateById(userRole);
        }

        log.info("成功为用户 {} 分配管理员角色", userId);
    }

    @Override
    @Transactional
    public void removeAdminRole(Integer userId) {
        log.info("开始移除用户 {} 的管理员角色", userId);
        
        // 检查是否是主管理员（ID为1）
        if (userId == 1) {
            log.error("尝试移除主管理员的管理员角色");
            throw new RuntimeException("不能移除主管理员的管理员权限");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(userId) == null) {
            log.error("用户 {} 不存在", userId);
            throw new RuntimeException("用户不存在");
        }

        // 获取普通用户角色ID（应该是2）
        Role userRole = roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, "ROLE_USER")
        );

        if (userRole == null) {
            log.error("普通用户角色不存在");
            throw new RuntimeException("普通用户角色不存在");
        }

        // 更新用户的角色
        UserRole currentRole = userRoleMapper.selectOne(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
        );

        if (currentRole == null) {
            // 如果用户没有角色记录，创建一个
            currentRole = UserRole.builder()
                .userId(userId)
                .roleId(userRole.getRoleId())
                .createdAt(LocalDateTime.now())
                .build();
            userRoleMapper.insert(currentRole);
        } else {
            // 更新现有角色为普通用户
            currentRole.setRoleId(userRole.getRoleId());
            userRoleMapper.updateById(currentRole);
        }

        log.info("成功将用户 {} 降级为普通用户", userId);
    }

    @Override
    public boolean isAdmin(Integer userId) {
        log.info("检查用户 {} 是否为管理员", userId);
        boolean isAdmin = roleMapper.hasAdminRole(userId);
        log.info("用户 {} {}是管理员", userId, isAdmin ? "" : "不");
        return isAdmin;
    }
} 