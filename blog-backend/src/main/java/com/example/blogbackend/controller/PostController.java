package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.CommentService;
import com.example.blogbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody Post post
    ) {
        try {
            log.info("接收到创建文章请求，用户ID: {}, 标题: {}", user.getUserId(), post.getTitle());
            
            // 确保 tags 字段正确设置
            if (post.getTags() == null) {
                post.setTags("");
            }
            
            post.setUserId(user.getUserId());
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            Post createdPost = postService.createPost(post);
            log.info("文章创建成功，ID: {}", createdPost.getPostId());
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            log.error("创建文章失败: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "创建文章失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag
    ) {
        try {
            // 保证分页参数合法
            page = Math.max(1, page);
            pageSize = Math.max(1, pageSize);
            
            log.info("接收到获取文章列表请求，页码：{}，每页数量：{}，关键词：{}，标签：{}", page, pageSize, keyword, tag);
            List<Post> posts;
            int totalCount;
            
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 关键词搜索
                posts = postService.searchPosts(keyword, page, pageSize);
                // 这里简化处理，实际可能需要单独查询总数
                totalCount = posts.size() * page; // 估算值
            } else if (tag != null && !tag.trim().isEmpty()) {
                // 标签筛选
                posts = postService.getPostsByTag(tag, offset, pageSize);
                // 这里简化处理，实际可能需要单独查询总数
                totalCount = posts.size() * page; // 估算值
            } else {
                // 获取所有文章
                posts = postService.getAllPosts();
                totalCount = posts.size();
                
                // 在内存中分页
                if (offset < posts.size()) {
                    int end = Math.min(offset + pageSize, posts.size());
                    posts = posts.subList(offset, end);
                } else {
                    posts = new ArrayList<>();
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("items", posts);
            response.put("total", totalCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取文章列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("获取文章列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Integer postId) {
        try {
            log.info("接收到获取文章详情请求，文章ID: {}", postId);
            
            // 获取文章详情
            Post post = postService.getPostById(postId);
            log.info("获取到文章: ID={}, 标题={}, 用户ID={}", post.getPostId(), post.getTitle(), post.getUserId());
            
            // 获取文章评论
            List<Comment> comments = commentService.findCommentsByPostIdWithUser(postId);
            log.info("文章评论获取完成，共{}条评论", comments.size());
            
            // 记录每条评论的详细信息
            for (Comment comment : comments) {
                log.info("评论详情: ID={}, 内容={}, 用户={}, 时间={}", 
                        comment.getCommentId(), 
                        comment.getContent(),
                        comment.getUserId(),
                        comment.getCreatedAt());
                
                if (comment.getUser() != null) {
                    log.info("评论用户信息: 用户名={}, 头像={}", 
                            comment.getUser().getUsername(), 
                            comment.getUser().getAvatar());
                } else {
                    log.warn("评论用户信息为空: 评论ID={}", comment.getCommentId());
                }
            }
            
            // 设置评论到文章对象
            post.setComments(comments);
            
            // 计算评论数量
            post.setCommentCount(comments.size());
            
            log.info("文章详情获取成功，文章ID: {}, 评论数: {}", post.getPostId(), comments.size());
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("获取文章详情失败: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Integer userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId,
            @Valid @RequestBody Post post
    ) {
        // 检查用户是否为空
        if (user == null) {
            log.error("更新文章失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        post.setPostId(postId);
        post.setUserId(user.getUserId());
        Post updatedPost = postService.updatePost(post);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        // 检查用户是否为空
        if (user == null) {
            log.error("删除文章失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        postService.deletePost(postId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        try {
            log.info("接收到获取所有标签请求");
            List<String> tags = postService.getAllTags();
            log.info("成功获取标签列表，共{}个标签", tags.size());
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            log.error("获取标签列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/bytags")
    public ResponseEntity<List<Post>> getPostsByTags(
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        try {
            // 保证分页参数合法
            page = Math.max(1, page);
            pageSize = Math.max(1, pageSize);
            
            log.info("接收到按标签获取文章请求，标签: {}, 页码: {}, 每页数量: {}", tags, page, pageSize);
            List<String> tagList = tags == null ? new ArrayList<>() : 
                                 Arrays.asList(tags.split(","));
            List<Post> posts = postService.getPostsByTags(tagList, page, pageSize);
            log.info("成功获取文章列表，共{}篇文章", posts.size());
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("按标签获取文章列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId,
            @RequestBody Map<String, String> payload
    ) {
        try {
            // 从请求体中获取评论内容
            String content = payload.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            log.info("接收到添加评论请求，用户ID: {}, 文章ID: {}", user.getUserId(), postId);
            
            // 创建评论对象
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(user.getUserId());
            comment.setContent(content);
            
            // 保存评论
            Comment createdComment = commentService.createComment(comment);
            log.info("评论创建成功，ID: {}", createdComment.getCommentId());
            
            return ResponseEntity.ok(createdComment);
        } catch (Exception e) {
            log.error("添加评论失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Integer postId) {
        try {
            log.info("接收到获取评论请求，文章ID: {}", postId);
            List<Comment> comments = commentService.findCommentsByPostIdWithUser(postId);
            log.info("获取到{}条评论", comments.size());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("获取评论失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 