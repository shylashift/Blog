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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @Value("${upload.path}")
    private String uploadPath;

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

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("用户未登录");
        }

        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("只能上传图片文件");
            }

            // 检查文件大小（最大2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("图片大小不能超过2MB");
            }

            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "unknown.jpg";
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 更新用户头像URL
            String avatarUrl = "/uploads/" + filename;
            user.setAvatar(avatarUrl);
            userService.updateUser(user);

            // 返回头像URL和状态码
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", avatarUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("上传头像失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "上传头像失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 