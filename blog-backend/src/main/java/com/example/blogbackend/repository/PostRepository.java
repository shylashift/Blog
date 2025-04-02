package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PostRepository extends BaseMapper<Post> {
    @Select("SELECT p.*, u.Username as \"user.username\", u.Avatar as \"user.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.PostId = #{postId}")
    Post findPostWithUser(Integer postId);
    
    @Select("SELECT p.*, u.Username as \"user.username\", u.Avatar as \"user.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> findAllWithUser();
    
    @Select("SELECT p.*, u.Username as \"user.username\", u.Avatar as \"user.avatar\" " +
           "FROM Posts p " +
           "LEFT JOIN Users u ON p.UserId = u.UserId " +
           "WHERE p.UserId = #{userId} " +
           "ORDER BY p.CreatedAt DESC")
    List<Post> findByUserId(Integer userId);

    @Select("SELECT DISTINCT tag FROM post_tags")
    List<String> findAllTags();
} 