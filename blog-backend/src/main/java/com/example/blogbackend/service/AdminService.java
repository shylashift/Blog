package com.example.blogbackend.service;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 为用户分配管理员角色
     * @param userId 用户ID
     * @throws RuntimeException 如果用户不存在
     */
    void assignAdminRole(Integer userId);

    /**
     * 移除用户的管理员角色
     * @param userId 用户ID
     */
    void removeAdminRole(Integer userId);

    /**
     * 检查用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Integer userId);
} 