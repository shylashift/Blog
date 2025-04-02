package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentRepository extends BaseMapper<Comment> {
    @Select("SELECT c.*, u.Username as \"user.username\", u.Avatar as \"user.avatar\" " +
           "FROM Comments c " +
           "LEFT JOIN Users u ON c.UserId = u.UserId " +
           "WHERE c.PostId = #{postId} " +
           "ORDER BY c.CreatedAt DESC")
    List<Comment> findByPostIdWithUser(Integer postId);
} 