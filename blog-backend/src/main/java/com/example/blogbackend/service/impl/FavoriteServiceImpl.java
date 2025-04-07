package com.example.blogbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogbackend.entity.Favorite;
import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.exception.ResourceNotFoundException;
import com.example.blogbackend.mapper.FavoriteMapper;
import com.example.blogbackend.mapper.MessageMapper;
import com.example.blogbackend.mapper.PostMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.service.FavoriteService;
import com.example.blogbackend.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public boolean checkFavorite(Integer userId, Integer postId) {
        log.info("开始检查用户是否收藏文章，用户ID: {}, 文章ID: {}", userId, postId);
        long startTime = System.currentTimeMillis();
        try {
            return favoriteMapper.findByUserIdAndPostId(userId, postId).isPresent();
        } finally {
            log.info("检查收藏状态完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    public void addFavorite(Integer userId, Integer postId) {
        log.info("开始添加收藏，用户ID: {}, 文章ID: {}", userId, postId);
        long startTime = System.currentTimeMillis();
        try {
            // 检查是否已经收藏
            if (favoriteMapper.findByUserIdAndPostId(userId, postId).isPresent()) {
                log.warn("用户已经收藏过此文章");
                return;
            }

            // 检查文章是否存在
            Post post = postMapper.findPostWithUser(postId);
            if (post == null) {
                log.error("文章不存在，ID: {}", postId);
                throw new RuntimeException("文章不存在");
            }

            // 创建收藏
            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .postId(postId)
                    .createdAt(LocalDateTime.now())
                    .build();
            favoriteMapper.insert(favorite);

            // 创建消息通知文章作者
            if (!post.getUserId().equals(userId)) {
                Message message = Message.builder()
                        .userId(post.getUserId())  // 接收消息的用户
                        .postId(postId)           // 相关文章
                        .type("favorite")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                messageMapper.insert(message);
                log.info("已创建收藏通知消息，接收者ID: {}", post.getUserId());
            }

            log.info("收藏添加成功");
        } catch (Exception e) {
            log.error("添加收藏失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加收藏失败: " + e.getMessage());
        } finally {
            log.info("添加收藏完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    public void removeFavorite(Integer userId, Integer postId) {
        log.info("开始取消收藏，用户ID: {}, 文章ID: {}", userId, postId);
        long startTime = System.currentTimeMillis();
        try {
            // 检查收藏是否存在
            Favorite favorite = favoriteMapper.findByUserIdAndPostId(userId, postId)
                    .orElseThrow(() -> new RuntimeException("未收藏此文章"));

            favoriteMapper.deleteById(favorite.getFavoriteId());
            log.info("取消收藏成功");
        } catch (Exception e) {
            log.error("取消收藏失败: {}", e.getMessage(), e);
            throw new RuntimeException("取消收藏失败: " + e.getMessage());
        } finally {
            log.info("取消收藏完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Favorite> getUserFavorites(Integer userId) {
        log.info("开始获取用户收藏列表，用户ID: {}", userId);
        long startTime = System.currentTimeMillis();
        try {
            return favoriteMapper.findByUserId(userId);
        } finally {
            log.info("获取用户收藏列表完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getFavoriteCount(Integer postId) {
        log.info("开始获取文章收藏数量，文章ID: {}", postId);
        long startTime = System.currentTimeMillis();
        try {
            return favoriteMapper.countByPostId(postId);
        } finally {
            log.info("获取文章收藏数量完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Favorite> getPostFavorites(Integer postId) {
        log.info("开始获取文章收藏用户列表，文章ID: {}", postId);
        long startTime = System.currentTimeMillis();
        try {
            return favoriteMapper.findByPostId(postId);
        } finally {
            log.info("获取文章收藏用户列表完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    private FavoriteVO convertToVO(Favorite favorite) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getFavoriteId());
        vo.setPostId(favorite.getPostId());
        vo.setTitle(favorite.getPost().getTitle());
        vo.setAuthor(favorite.getUser().getUsername());
        vo.setCreatedAt(favorite.getCreatedAt());
        return vo;
    }
} 