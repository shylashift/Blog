package com.example.blogbackend.service;

import com.example.blogbackend.dto.AuthenticationRequest;
import com.example.blogbackend.dto.AuthenticationResponse;
import com.example.blogbackend.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthenticationService {
    /**
     * 用户注册
     * @param request 注册请求
     * @return 认证响应（包含token和用户信息）
     * @throws RuntimeException 如果用户名或邮箱已存在
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * 用户登录认证
     * @param request 认证请求
     * @return 认证响应（包含token和用户信息）
     * @throws RuntimeException 如果用户不存在或密码错误
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * 重置密码
     * @param email 用户邮箱
     * @param newPassword 新密码
     * @throws RuntimeException 如果用户不存在
     */
    void resetPassword(String email, String newPassword);
} 