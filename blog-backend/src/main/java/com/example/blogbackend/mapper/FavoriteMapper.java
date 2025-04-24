package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    /**
     * 查找用户对文章的收藏
     * @param userId 用户ID
     * @param postId 文章ID
     * @return 收藏信息
     */
    @Select("SELECT f.*, p.Title as \"post.title\", " +
           "u.Username as \"user.username\" " +
           "FROM Favorites f " +
           "LEFT JOIN Users u ON f.UserId = u.UserId " +
           "LEFT JOIN Posts p ON f.PostId = p.PostId " +
           "WHERE f.UserId = #{userId} AND f.PostId = #{postId}")
    Optional<Favorite> findByUserIdAndPostId(Integer userId, Integer postId);

    /**
     * 查找用户的所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    @Select("SELECT f.*, p.Title as \"post.title\", " +
           "u.Username as \"user.username\" " +
           "FROM Favorites f " +
           "LEFT JOIN Users u ON f.UserId = u.UserId " +
           "LEFT JOIN Posts p ON f.PostId = p.PostId " +
           "WHERE f.UserId = #{userId} " +
           "ORDER BY f.CreatedAt DESC")
    List<Favorite> findByUserId(Integer userId);

    /**
     * 查找文章的所有收藏
     * @param postId 文章ID
     * @return 收藏列表
     */
    @Select("SELECT f.*, p.Title as \"post.title\", " +
           "u.Username as \"user.username\" " +
           "FROM Favorites f " +
           "LEFT JOIN Users u ON f.UserId = u.UserId " +
           "LEFT JOIN Posts p ON f.PostId = p.PostId " +
           "WHERE f.PostId = #{postId} " +
           "ORDER BY f.CreatedAt DESC")
    List<Favorite> findByPostId(Integer postId);

    /**
     * 获取文章的收藏数量
     * @param postId 文章ID
     * @return 收藏数量
     */
    @Select("SELECT COUNT(*) FROM Favorites WHERE PostId = #{postId}")
    int countByPostId(Integer postId);
} 