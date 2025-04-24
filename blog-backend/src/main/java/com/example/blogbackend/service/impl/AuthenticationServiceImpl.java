package com.example.blogbackend.service.impl;

import com.example.blogbackend.dto.AuthenticationRequest;
import com.example.blogbackend.dto.AuthenticationResponse;
import com.example.blogbackend.dto.RegisterRequest;
import com.example.blogbackend.entity.Role;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.UserRole;
import com.example.blogbackend.mapper.RoleMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.mapper.UserRoleMapper;
import com.example.blogbackend.service.AuthenticationService;
import com.example.blogbackend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // 检查用户名和邮箱是否已存在
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已存在");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .bio(request.getBio())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);

        // 为新用户分配ROLE_USER角色
        var userRole = roleMapper.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        var userRoleRelation = UserRole.builder()
                .userId(user.getUserId())
                .roleId(userRole.getRoleId())
                .createdAt(LocalDateTime.now())
                .build();

        userRoleMapper.insert(userRoleRelation);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .roles(List.of("ROLE_USER"))
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // 先检查用户是否存在
            var user = userMapper.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 尝试认证
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 查询用户角色
            List<String> roles = userRoleMapper.findRolesByUserId(user.getUserId())
                    .stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());

            if (roles.isEmpty()) {
                roles = List.of("ROLE_USER"); // 默认角色
            }

            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .avatar(user.getAvatar())
                    .bio(user.getBio())
                    .roles(roles)
                    .build();
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new RuntimeException("密码错误");
        } catch (Exception e) {
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        var user = userMapper.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.updateById(user);
    }
} 