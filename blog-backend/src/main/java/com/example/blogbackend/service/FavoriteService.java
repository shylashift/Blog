package com.example.blogbackend.service;

import com.example.blogbackend.entity.Favorite;
import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService {
    /**
     * 检查用户是否收藏了文章
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 是否已收藏
     */
    boolean checkFavorite(Integer userId, Integer postId);

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param postId 文章ID
     */
    void addFavorite(Integer userId, Integer postId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param postId 文章ID
     */
    void removeFavorite(Integer userId, Integer postId);

    /**
     * 获取用户的所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> getUserFavorites(Integer userId);

    /**
     * 获取文章的收藏数量
     * @param postId 文章ID
     * @return 收藏数量
     */
    int getFavoriteCount(Integer postId);

    /**
     * 获取文章的收藏用户列表
     * @param postId 文章ID
     * @return 收藏用户列表
     */
    List<Favorite> getPostFavorites(Integer postId);
} 