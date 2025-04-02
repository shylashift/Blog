package com.example.blogbackend.service;

import com.example.blogbackend.entity.Favorite;
import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.repository.FavoriteRepository;
import com.example.blogbackend.repository.MessageRepository;
import com.example.blogbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Favorite addFavorite(Integer userId, Integer postId) {
        // 检查是否已经收藏
        if (favoriteRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("已经收藏过此文章");
        }

        // 检查文章是否存在
        Post post = postRepository.selectById(postId);
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }

        // 创建收藏
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepository.insert(favorite);

        // 创建消息通知文章作者
        if (!post.getUserId().equals(userId)) {
            Message message = Message.builder()
                    .userId(post.getUserId())
                    .postId(postId)
                    .type("favorite")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.insert(message);
        }

        return favorite;
    }

    @Transactional
    public void removeFavorite(Integer userId, Integer postId) {
        Favorite favorite = favoriteRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new RuntimeException("未收藏此文章"));
        favoriteRepository.deleteById(favorite.getFavoriteId());
    }

    @Transactional(readOnly = true)
    public List<Favorite> getUserFavorites(Integer userId) {
        return favoriteRepository.selectList(null);
    }

    @Transactional(readOnly = true)
    public boolean checkFavorite(Integer userId, Integer postId) {
        return favoriteRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }
} 