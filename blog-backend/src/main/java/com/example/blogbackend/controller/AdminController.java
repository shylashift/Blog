package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.service.AdminService;
import com.example.blogbackend.service.UserService;
import com.example.blogbackend.service.PostService;
import com.example.blogbackend.service.CommentService;
import com.example.blogbackend.mapper.PostMapper;
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
    private final PostMapper postMapper;
    private final CommentService commentService;

    /**
     * 提升用户为管理员
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Integer userId) {
        log.info("收到提升用户权限请求: userId={}", userId);
        adminService.assignAdminRole(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 降低管理员为普通用户
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<Void> demoteFromAdmin(@PathVariable Integer userId) {
        log.info("收到降低用户权限请求: userId={}", userId);
        adminService.removeAdminRole(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取所有用户列表（分页）
     */
    @PreAuthorize("hasAuthority('管理员')")
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
    @PreAuthorize("hasAuthority('管理员')")
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
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Integer userId) {
        log.info("收到启用用户请求: userId={}", userId);
        userService.enableUser(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取所有文章（分页）- 包括已删除和隐藏的文章
     */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        log.info("收到管理员获取所有文章请求: page={}, size={}, keyword={}", page, size, keyword);
        
        try {
            int offset = (page - 1) * size;
            List<Post> posts;
            int total;
            
            if (keyword != null && !keyword.isEmpty()) {
                posts = postMapper.searchPostsForAdmin(keyword, offset, size);
                total = postMapper.getPostCountByKeywordForAdmin(keyword);
            } else {
                posts = postMapper.findAllWithUserPaginatedForAdmin(offset, size);
                total = postMapper.selectCount(null).intValue();
            }
            
            // 添加状态标记
            for (Post post : posts) {
                if (post.getIsDeleted() != null && post.getIsDeleted()) {
                    post.setStatus("已删除");
                } else if (post.getIsHidden() != null && post.getIsHidden()) {
                    post.setStatus("已隐藏");
                } else {
                    post.setStatus("正常");
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", posts);
            response.put("total", total);
            
            log.info("管理员查询返回文章数量: {}, 总数: {}", posts.size(), total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("管理员获取文章列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取文章列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除文章（管理员权限）
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        log.info("收到管理员删除文章请求: postId={}", postId);
        postService.deletePostByAdmin(postId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 隐藏文章
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<Void> hidePost(@PathVariable Integer postId) {
        log.info("收到隐藏文章请求: postId={}", postId);
        postService.hidePost(postId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 显示文章
     */
    @PreAuthorize("hasAuthority('管理员')")
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

    /**
     * 获取仪表盘统计数据
     */
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', '管理员')")
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户总数
            int totalUsers = userService.getUserCount(null);
            
            // 获取文章总数
            int totalPosts = postMapper.selectCount(null).intValue();
            
            // 获取评论总数（如果有评论功能）
            int totalComments = 0; // TODO: 实现评论统计
            
            // 获取今日访问量（如果有访问统计功能）
            int todayVisits = 0; // TODO: 实现访问量统计
            
            stats.put("totalUsers", totalUsers);
            stats.put("totalPosts", totalPosts);
            stats.put("totalComments", totalComments);
            stats.put("todayVisits", todayVisits);
            
            log.info("统计数据: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return ResponseEntity.status(500).body(Map.of("error", "获取统计数据失败"));
        }
    }

    /**
     * 获取所有评论（分页）
     */
    @PreAuthorize("hasAuthority('管理员')")
    @GetMapping("/comments")
    public ResponseEntity<Map<String, Object>> getAllComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        log.info("收到获取所有评论请求: page={}, size={}, keyword={}", page, size, keyword);
        
        try {
            int offset = (page - 1) * size;
            List<Comment> comments = commentService.getAllCommentsWithDetails(keyword, offset, size);
            int total = commentService.getCommentCount(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", comments);
            response.put("total", total);
            
            log.info("返回评论数量: {}, 总数: {}", comments.size(), total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取评论列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取评论列表失败: " + e.getMessage()));
        }
    }

    /**
     * 删除评论
     */
    @PreAuthorize("hasAuthority('管理员')")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        log.info("收到删除评论请求: commentId={}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 隐藏评论
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<Void> hideComment(@PathVariable Integer commentId) {
        log.info("收到隐藏评论请求: commentId={}", commentId);
        commentService.hideComment(commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 显示评论
     */
    @PreAuthorize("hasAuthority('管理员')")
    @PostMapping("/comments/{commentId}/show")
    public ResponseEntity<Void> showComment(@PathVariable Integer commentId) {
        log.info("收到显示评论请求: commentId={}", commentId);
        commentService.showComment(commentId);
        return ResponseEntity.ok().build();
    }
} 