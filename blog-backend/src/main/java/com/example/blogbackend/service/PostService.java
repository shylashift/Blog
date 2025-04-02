package com.example.blogbackend.service;

import com.example.blogbackend.entity.Post;
import com.example.blogbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(Post post) {
        try {
            log.info("开始创建文章，标题: {}", post.getTitle());
            
            // 验证文章内容
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("文章标题不能为空");
            }
            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("文章内容不能为空");
            }
            
            // 设置时间
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            // 保存文章
            log.info("保存文章到数据库");
            int rows = postRepository.insert(post);
            if (rows != 1) {
                throw new RuntimeException("保存文章失败");
            }
            
            // 查询完整的文章信息
            Post createdPost = postRepository.findPostWithUser(post.getPostId());
            if (createdPost == null) {
                throw new RuntimeException("无法获取创建的文章信息");
            }
            
            log.info("文章创建成功，ID: {}", createdPost.getPostId());
            return createdPost;
        } catch (Exception e) {
            log.error("创建文章失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建文章失败: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        log.info("开始获取所有文章");
        long startTime = System.currentTimeMillis();
        try {
            return postRepository.findAllWithUser();
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            throw e;
        } finally {
            log.info("获取文章列表完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Transactional(readOnly = true)
    public Post getPostById(Integer postId) {
        return postRepository.findPostWithUser(postId);
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Integer userId) {
        return postRepository.findByUserId(userId);
    }

    @Transactional
    public Post updatePost(Post post) {
        Post existingPost = postRepository.selectById(post.getPostId());
        if (existingPost == null || !existingPost.getUserId().equals(post.getUserId())) {
            throw new RuntimeException("无权修改此文章");
        }
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.updateById(post);
        return postRepository.findPostWithUser(post.getPostId());
    }

    @Transactional
    public void deletePost(Integer postId, Integer userId) {
        Post post = postRepository.selectById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此文章");
        }
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        log.info("开始获取所有标签");
        long startTime = System.currentTimeMillis();
        try {
            return postRepository.findAllTags();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            throw e;
        } finally {
            log.info("获取标签列表完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }
} 