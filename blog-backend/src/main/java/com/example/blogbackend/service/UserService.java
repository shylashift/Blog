package com.example.blogbackend.service;

import com.example.blogbackend.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface UserService extends UserDetailsService {
    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     * @throws UsernameNotFoundException 如果用户不存在
     */
    User getUserById(Integer userId);

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 用户邮箱
     * @return 用户信息
     * @throws UsernameNotFoundException 如果用户不存在
     */
    User getUserByEmail(String email);

    /**
     * 更新用户信息
     *
     * @param user 要更新的用户信息
     * @return 更新后的用户信息
     * @throws UsernameNotFoundException 如果用户不存在
     */
    User updateUser(User user);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 要检查的邮箱
     * @return true如果邮箱已存在，false否则
     */
    boolean isEmailExists(String email);

    /**
     * 检查用户名是否已存在
     *
     * @param username 要检查的用户名
     * @return true如果用户名已存在，false否则
     */
    boolean isUsernameExists(String username);

    /**
     * 获取所有用户（包含角色信息）
     *
     * @param keyword 搜索关键词（用户名或邮箱）
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 用户列表
     */
    List<User> getAllUsersWithRoles(String keyword, int offset, int limit);

    /**
     * 获取用户总数
     *
     * @param keyword 搜索关键词（用户名或邮箱）
     * @return 用户总数
     */
    int getUserCount(String keyword);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @throws UsernameNotFoundException 如果用户不存在
     */
    void disableUser(Integer userId);

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @throws UsernameNotFoundException 如果用户不存在
     */
    void enableUser(Integer userId);
} 