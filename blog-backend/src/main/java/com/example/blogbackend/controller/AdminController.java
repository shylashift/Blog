package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.service.AdminService;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final PostService postService;

    /**
     * 提升用户为管理员
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Integer userId) {
        log.info("收到提升用户权限请求: userId={}", userId);
        adminService.assignAdminRole(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 降低管理员为普通用户
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<Void> demoteFromAdmin(@PathVariable Integer userId) {
        log.info("收到降低用户权限请求: userId={}", userId);
        adminService.removeAdminRole(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取所有用户列表（分页）
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        log.info("收到获取所有用户请求: page={}, size={}, keyword={}", page, size, keyword);
        
        int offset = (page - 1) * size;
        List<User> users = userService.getAllUsersWithRoles(keyword, offset, size);
        int total = userService.getUserCount(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", users);
        response.put("total", total);
        
        log.info("返回用户数量: {}, 总数: {}", users.size(), total);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 禁用用户
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<Void> disableUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Integer userId
    ) {
        // 检查用户是否为空
        if (currentUser == null) {
            log.error("禁用用户失败: 管理员未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        // 不能禁用自己
        if (currentUser.getUserId().equals(userId)) {
            log.warn("用户尝试禁用自己的账号: {}", userId);
            return ResponseEntity.badRequest().build();
        }
        
        log.info("收到禁用用户请求: userId={}", userId);
        userService.disableUser(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 启用用户
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Integer userId) {
        log.info("收到启用用户请求: userId={}", userId);
        userService.enableUser(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取所有文章（分页）
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        log.info("收到获取所有文章请求: page={}, size={}, keyword={}", page, size, keyword);
        
        int offset = (page - 1) * size;
        List<Post> posts;
        int total;
        
        if (keyword != null && !keyword.isEmpty()) {
            posts = postService.searchPosts(keyword, offset, size);
            total = postService.getPostCount(keyword);
        } else {
            posts = postService.getAllPostsWithPagination(offset, size);
            total = postService.getPostCount(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", posts);
        response.put("total", total);
        
        log.info("返回文章数量: {}, 总数: {}", posts.size(), total);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除文章（管理员权限）
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        log.info("收到管理员删除文章请求: postId={}", postId);
        postService.deletePostByAdmin(postId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 隐藏文章
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<Void> hidePost(@PathVariable Integer postId) {
        log.info("收到隐藏文章请求: postId={}", postId);
        postService.hidePost(postId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 显示文章
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/posts/{postId}/show")
    public ResponseEntity<Void> showPost(@PathVariable Integer postId) {
        log.info("收到显示文章请求: postId={}", postId);
        postService.showPost(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 管理员自检API，确保admin用户有管理员权限
     */
    @GetMapping("/checkAdminRole")
    public ResponseEntity<Map<String, Object>> checkAdminRole(
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser == null) {
            log.error("管理员权限自检失败: 用户未登录");
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        log.info("管理员权限自检: userId={}, email={}, 当前权限: {}", 
                currentUser.getUserId(), currentUser.getEmail(), currentUser.getAuthorities());

        try {
            // 检查用户是否有管理员角色
            boolean hasAdminRole = adminService.isAdmin(currentUser.getUserId());
            log.info("用户 {} 是否有管理员角色: {}", currentUser.getEmail(), hasAdminRole);

            // 如果是admin用户且没有管理员角色，自动修复
            if (currentUser.getEmail().equals("admin@example.com") && !hasAdminRole) {
                log.warn("发现admin用户没有管理员角色，开始自动修复");
                adminService.assignAdminRole(currentUser.getUserId());
                hasAdminRole = true;
                log.info("admin用户权限修复完成");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("username", currentUser.getUsername());
            response.put("email", currentUser.getEmail());
            response.put("isAdmin", hasAdminRole);
            response.put("roles", currentUser.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(java.util.stream.Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("管理员权限检查过程中发生错误", e);
            return ResponseEntity.status(500).body(Map.of("message", "权限检查失败: " + e.getMessage()));
        }
    }
} 