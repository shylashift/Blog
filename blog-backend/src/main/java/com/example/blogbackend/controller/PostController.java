package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody Post post
    ) {
        try {
            log.info("接收到创建文章请求，用户ID: {}, 标题: {}", user.getUserId(), post.getTitle());
            post.setUserId(user.getUserId());
            Post createdPost = postService.createPost(post);
            log.info("文章创建成功，ID: {}", createdPost.getPostId());
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            log.error("创建文章失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("创建文章失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Integer postId) {
        Post post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
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
        postService.deletePost(postId, user.getUserId());
        return ResponseEntity.ok().build();
    }
} 