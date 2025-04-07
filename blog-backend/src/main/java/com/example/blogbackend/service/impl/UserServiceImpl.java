package com.example.blogbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Instant start = Instant.now();
        log.info("开始根据用户名加载用户: {}", username);
        
        try {
            return userMapper.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("加载用户完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        Instant start = Instant.now();
        log.info("开始根据ID获取用户信息: {}", userId);
        
        try {
            return userMapper.findByIdWithRoles(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在，ID: " + userId));
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户信息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        Instant start = Instant.now();
        log.info("开始根据邮箱获取用户信息: {}", email);
        
        try {
            return userMapper.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在，邮箱: " + email));
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户信息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        Instant start = Instant.now();
        log.info("开始更新用户信息，用户ID: {}", user.getUserId());
        
        try {
            user.setUpdatedAt(LocalDateTime.now());
            int result = userMapper.updateById(user);
            if (result != 1) {
                throw new RuntimeException("更新用户信息失败，用户ID: " + user.getUserId());
            }
            log.info("用户信息更新成功");
            return userMapper.findByIdWithRoles(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("无法获取更新后的用户信息"));
        } catch (Exception e) {
            log.error("更新用户信息时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("更新用户信息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        Instant start = Instant.now();
        log.info("开始检查邮箱是否存在: {}", email);
        
        try {
            int count = userMapper.countByEmail(email);
            return count > 0;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("检查邮箱完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        Instant start = Instant.now();
        log.info("开始检查用户名是否存在: {}", username);
        
        try {
            int count = userMapper.countByUsername(username);
            return count > 0;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("检查用户名完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsersWithRoles(String keyword, int offset, int limit) {
        Instant start = Instant.now();
        log.info("开始获取所有用户列表，关键词: {}, 偏移量: {}, 限制数量: {}", keyword, offset, limit);
        
        try {
            List<User> users;
            if (keyword != null && !keyword.isEmpty()) {
                users = userMapper.findByKeywordWithRoles(keyword, offset, limit);
            } else {
                users = userMapper.findAllWithRoles(offset, limit);
            }
            log.info("获取到{}个用户", users.size());
            return users;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户列表完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getUserCount(String keyword) {
        Instant start = Instant.now();
        log.info("开始获取用户总数，关键词: {}", keyword);
        
        try {
            int count;
            if (keyword != null && !keyword.isEmpty()) {
                count = userMapper.countByKeyword(keyword);
            } else {
                count = userMapper.selectCount(null).intValue();
            }
            log.info("用户总数为: {}", count);
            return count;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户总数完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional
    public void disableUser(Integer userId) {
        Instant start = Instant.now();
        log.info("开始禁用用户，ID: {}", userId);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，ID: {}", userId);
                throw new UsernameNotFoundException("用户不存在，ID: " + userId);
            }
            
            user.setDisabled(true);
            user.setUpdatedAt(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            if (result != 1) {
                throw new RuntimeException("禁用用户失败");
            }
            log.info("用户已禁用");
        } catch (Exception e) {
            log.error("禁用用户时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("禁用用户完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional
    public void enableUser(Integer userId) {
        Instant start = Instant.now();
        log.info("开始启用用户，ID: {}", userId);
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，ID: {}", userId);
                throw new UsernameNotFoundException("用户不存在，ID: " + userId);
            }
            
            user.setDisabled(false);
            user.setUpdatedAt(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            if (result != 1) {
                throw new RuntimeException("启用用户失败");
            }
            log.info("用户已启用");
        } catch (Exception e) {
            log.error("启用用户时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("启用用户完成，耗时: {}ms", duration.toMillis());
        }
    }
} 