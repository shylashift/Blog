package com.example.blogbackend.service;

import com.example.blogbackend.entity.Comment;
import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    /**
     * 创建新评论
     * @param comment 评论信息
     * @return 创建的评论
     */
    Comment createComment(Comment comment);

    /**
     * 获取文章的所有评论
     * @param postId 文章ID
     * @return 评论列表
     */
    List<Comment> getCommentsByPostId(Integer postId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteComment(Integer commentId, Integer userId);

    /**
     * 获取用户的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    List<Comment> getCommentsByUserId(Integer userId);

    /**
     * 更新评论
     * @param comment 评论信息
     * @return 更新后的评论
     */
    Comment updateComment(Comment comment);

    /**
     * 获取文章的所有评论，包含用户信息
     * @param postId 文章ID
     * @return 评论列表（包含用户信息）
     */
    List<Comment> findCommentsByPostIdWithUser(Integer postId);

    /**
     * 获取所有评论（带用户和文章信息）
     */
    List<Comment> getAllCommentsWithDetails(String keyword, int offset, int size);

    /**
     * 获取评论总数
     */
    int getCommentCount(String keyword);

    /**
     * 删除评论
     */
    void deleteComment(Integer commentId);

    /**
     * 隐藏评论
     */
    void hideComment(Integer commentId);

    /**
     * 显示评论
     */
    void showComment(Integer commentId);
} 