package com.example.blogbackend.service.impl;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.mapper.PostMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.service.PostService;
import com.example.blogbackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Post createPost(Post post) {
        Instant start = Instant.now();
        log.info("开始创建文章: {}", post.getTitle());
        
        try {
            // 检查用户是否被禁用
            User user = userMapper.selectById(post.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            if (user.getDisabled() != null && user.getDisabled()) {
                log.error("用户已被禁用，不能发表文章，用户ID: {}", post.getUserId());
                throw new RuntimeException("您的账号已被禁用，暂时无法发表文章");
            }

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
            int result = postMapper.insert(post);
            if (result != 1) {
                throw new RuntimeException("创建文章失败");
            }
            
            // 返回完整的文章信息
            Post createdPost = postMapper.findPostWithUser(post.getPostId());
            log.info("文章创建成功，ID: {}", createdPost.getPostId());
            return createdPost;
        } catch (Exception e) {
            log.error("创建文章时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("创建文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        Instant start = Instant.now();
        log.info("开始获取所有文章");
        
        try {
            // 使用findAllWithUser替代selectList，以便获取作者信息
            List<Post> posts = postMapper.findAllWithUser();
            log.info("获取到{}篇文章", posts.size());
            return posts;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取所有文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Integer postId) {
        Instant start = Instant.now();
        log.info("开始获取文章，ID: {}", postId);
        
        try {
            // 使用findPostWithUser替代selectById，以便获取作者信息
            Post post = postMapper.findPostWithUser(postId);
            if (post == null) {
                log.warn("文章不存在，ID: {}", postId);
                throw new RuntimeException("文章不存在");
            }
            return post;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取用户的文章，用户ID: {}", userId);
        
        try {
            List<Post> posts = postMapper.getPostsByUserId(userId);
            log.info("获取到用户{}的{}篇文章", userId, posts.size());
            return posts;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public Post updatePost(Post post) {
        Instant start = Instant.now();
        log.info("开始更新文章，ID: {}", post.getPostId());
        
        try {
            // 检查文章是否存在
            Post existingPost = postMapper.findPostWithUser(post.getPostId());
            if (existingPost == null || !existingPost.getUserId().equals(post.getUserId())) {
                log.error("无权修改此文章，ID: {}", post.getPostId());
                throw new RuntimeException("无权修改此文章");
            }

            // 更新时间
            post.setUpdatedAt(LocalDateTime.now());
            
            // 更新文章
            int result = postMapper.updateById(post);
            if (result != 1) {
                throw new RuntimeException("更新文章失败");
            }
            
            // 返回更新后的文章
            return postMapper.findPostWithUser(post.getPostId());
        } catch (Exception e) {
            log.error("更新文章时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("更新文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public void deletePost(Integer postId) {
        // 先检查文章是否存在
        Post post = postMapper.findPostWithUser(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }

        // 检查当前用户是否有权限删除
        User currentUser = SecurityUtils.getCurrentUser();
        if (!currentUser.getRoles().contains("ROLE_ADMIN") && !post.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("没有权限删除此文章");
        }

        try {
            // 删除文章
            postMapper.deletePost(postId);
            log.info("文章删除成功，ID: {}", postId);
        } catch (Exception e) {
            log.error("删除文章失败: {}", e.getMessage());
            throw new RuntimeException("删除文章失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        Instant start = Instant.now();
        log.info("开始获取所有标签");
        
        try {
            List<String> tags = postMapper.getAllTags();
            log.info("获取到{}个标签", tags.size());
            return tags;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取所有标签完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByTags(List<String> tags, int page, int pageSize) {
        Instant start = Instant.now();
        log.info("开始根据标签获取文章，标签: {}, 页码: {}, 每页数量: {}", tags, page, pageSize);
        
        try {
            List<Post> result = new ArrayList<>();
            int offset = (page - 1) * pageSize;
            
            for (String tag : tags) {
                List<Post> posts = postMapper.getPostsByTag(tag, offset, pageSize);
                result.addAll(posts);
            }
            
            log.info("获取到{}篇文章", result.size());
            return result;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("根据标签获取文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsByTag(String tag, int offset, int limit) {
        Instant start = Instant.now();
        log.info("开始根据单个标签获取文章，标签: {}", tag);
        
        try {
            // 确保分页参数有效
            offset = Math.max(0, offset);
            limit = Math.max(1, limit);
            
            List<Post> posts = postMapper.getPostsByTag(tag, 0, 0);
            log.info("获取到{}篇文章", posts.size());
            
            // 手动分页，避免SQL服务器复杂分页问题
            if (posts.size() > offset) {
                int endIndex = Math.min(offset + limit, posts.size());
                return posts.subList(offset, endIndex);
            }
            return new ArrayList<>();
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("根据单个标签获取文章完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> searchPosts(String keyword, int page, int size) {
        Instant start = Instant.now();
        log.info("开始搜索文章，关键词: {}, 页码: {}, 每页数量: {}", keyword, page, size);
        
        try {
            // 确保页码从1开始
            if (page < 1) {
                page = 1;
            }
            // 计算偏移量
            int offset = (page - 1) * size;
            List<Post> posts = postMapper.searchPosts(keyword, offset, size);
            log.info("搜索到{}篇文章", posts.size());
            return posts;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("搜索文章完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getPostCount(String keyword) {
        Instant start = Instant.now();
        log.info("开始获取文章总数，关键词: {}", keyword);
        
        try {
            int count;
            if (keyword != null && !keyword.isEmpty()) {
                // 搜索时的总数
                count = postMapper.getPostCountByKeyword(keyword);
            } else {
                // 所有文章总数
                count = postMapper.selectCount(null).intValue();
            }
            log.info("文章总数为: {}", count);
            return count;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取文章总数完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Post> getAllPostsWithPagination(int offset, int limit) {
        Instant start = Instant.now();
        log.info("开始获取分页文章，偏移量: {}, 限制数量: {}", offset, limit);
        
        try {
            List<Post> posts = postMapper.findAllWithUserPaginated(offset, limit);
            log.info("获取到{}篇文章", posts.size());
            return posts;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取分页文章完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional
    public void deletePostByAdmin(Integer postId) {
        Instant start = Instant.now();
        log.info("管理员开始删除文章，ID: {}", postId);
        
        try {
            // 检查文章是否存在
            Post post = postMapper.findPostWithUser(postId);
            if (post == null) {
                log.warn("文章不存在，ID: {}", postId);
                throw new RuntimeException("文章不存在");
            }
            
            // 使用软删除
            postMapper.deletePost(postId);
            log.info("管理员删除文章成功");
        } catch (Exception e) {
            log.error("管理员删除文章时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("管理员删除文章完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional
    public void hidePost(Integer postId) {
        Instant start = Instant.now();
        log.info("开始隐藏文章，ID: {}", postId);
        
        try {
            // 检查文章是否存在
            Post post = postMapper.findPostWithUser(postId);
            if (post == null) {
                log.warn("文章不存在，ID: {}", postId);
                throw new RuntimeException("文章不存在");
            }
            
            // 更新隐藏状态
            post.setIsHidden(true);
            post.setUpdatedAt(LocalDateTime.now());
            
            int result = postMapper.updateById(post);
            if (result != 1) {
                throw new RuntimeException("隐藏文章失败");
            }
            log.info("文章隐藏成功");
        } catch (Exception e) {
            log.error("隐藏文章时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("隐藏文章完成，耗时: {}ms", duration.toMillis());
        }
    }
    
    @Override
    @Transactional
    public void showPost(Integer postId) {
        Instant start = Instant.now();
        log.info("开始显示文章，ID: {}", postId);
        
        try {
            // 检查文章是否存在
            Post post = postMapper.findPostWithUser(postId);
            if (post == null) {
                log.warn("文章不存在，ID: {}", postId);
                throw new RuntimeException("文章不存在");
            }
            
            // 更新隐藏状态
            post.setIsHidden(false);
            post.setUpdatedAt(LocalDateTime.now());
            
            int result = postMapper.updateById(post);
            if (result != 1) {
                throw new RuntimeException("显示文章失败");
            }
            log.info("文章显示成功");
        } catch (Exception e) {
            log.error("显示文章时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("显示文章完成，耗时: {}ms", duration.toMillis());
        }
    }
} 