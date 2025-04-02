package com.example.blogbackend.service;

import com.example.blogbackend.entity.UserRole;
import com.example.blogbackend.repository.RoleRepository;
import com.example.blogbackend.repository.UserRepository;
import com.example.blogbackend.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public void promoteToAdmin(Integer userId) {
        // 检查用户是否存在
        var user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否已经是管理员
        var adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));

        var existingRole = userRoleRepository.findByUserIdAndRoleId(userId, adminRole.getRoleId());
        if (existingRole.isPresent()) {
            throw new RuntimeException("用户已经是管理员");
        }

        // 添加管理员角色
        var userRoleRelation = UserRole.builder()
                .userId(userId)
                .roleId(adminRole.getRoleId())
                .createdAt(LocalDateTime.now())
                .build();

        userRoleRepository.insert(userRoleRelation);
    }

    @Transactional
    public void demoteFromAdmin(Integer userId) {
        // 检查用户是否存在
        var user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否是管理员
        var adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));

        var existingRole = userRoleRepository.findByUserIdAndRoleId(userId, adminRole.getRoleId())
                .orElseThrow(() -> new RuntimeException("用户不是管理员"));

        // 移除管理员角色
        userRoleRepository.deleteById(existingRole.getUserRoleId());
    }
} 