package com.example.blogbackend.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface MessageRepository extends BaseMapper<Message> {
    @Select("SELECT m.*, p.Title as \"post.title\", c.Content as \"comment.content\", " +
           "u.Username as \"user.username\", u.Avatar as \"user.avatar\" " +
           "FROM Messages m " +
           "LEFT JOIN Posts p ON m.PostId = p.PostId " +
           "LEFT JOIN Comments c ON m.CommentId = c.CommentId " +
           "LEFT JOIN Users u ON m.UserId = u.UserId " +
           "WHERE m.UserId = #{userId} AND m.IsRead = 0 " +
           "ORDER BY m.CreatedAt DESC")
    List<Message> findUnreadByUserId(Integer userId);
    
    @Update("UPDATE Messages SET IsRead = 1 WHERE MessageId = #{messageId}")
    int markAsRead(Integer messageId);
} 