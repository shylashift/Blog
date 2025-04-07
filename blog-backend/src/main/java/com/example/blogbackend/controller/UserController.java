package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("获取当前用户信息失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(
            @AuthenticationPrincipal User currentUser,
            @RequestBody User updateRequest
    ) {
        if (currentUser == null) {
            log.error("更新用户信息失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        updateRequest.setUserId(currentUser.getUserId());
        User updatedUser = userService.updateUser(updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getUserPosts(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("获取用户文章列表失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        log.info("获取用户文章列表，用户ID: {}", user.getUserId());
        try {
            List<Post> posts = postService.getPostsByUserId(user.getUserId());
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("获取用户文章列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 