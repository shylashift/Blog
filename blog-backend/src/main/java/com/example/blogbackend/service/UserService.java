package com.example.blogbackend.service;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("开始加载用户信息: {}", email);
        long startTime = System.currentTimeMillis();
        try {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        } finally {
            log.info("加载用户信息完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        log.info("开始获取用户信息: {}", userId);
        long startTime = System.currentTimeMillis();
        try {
            return userRepository.selectById(userId);
        } finally {
            log.info("获取用户信息完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Transactional
    public User updateUser(User user) {
        log.info("开始更新用户信息: {}", user.getUserId());
        long startTime = System.currentTimeMillis();
        try {
            User existingUser = userRepository.selectById(user.getUserId());
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }
            userRepository.updateById(user);
            return userRepository.selectById(user.getUserId());
        } finally {
            log.info("更新用户信息完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }
} 