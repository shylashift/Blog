package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.mapper.CommentMapper;
import com.example.blogbackend.mapper.MessageMapper;
import com.example.blogbackend.mapper.PostMapper;
import com.example.blogbackend.mapper.UserMapper;
import com.example.blogbackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        log.info("开始创建评论，文章ID: {}, 用户ID: {}, 内容: {}", 
                comment.getPostId(), comment.getUserId(), comment.getContent());
        
        long startTime = System.currentTimeMillis();
        try {
            // 检查评论参数
            if (comment.getPostId() == null) {
                log.error("文章ID不能为空");
                throw new IllegalArgumentException("文章ID不能为空");
            }
            if (comment.getUserId() == null) {
                log.error("用户ID不能为空");
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 检查文章是否存在
            Post post = postMapper.selectById(comment.getPostId());
            if (post == null) {
                log.error("文章不存在，ID: {}", comment.getPostId());
                throw new RuntimeException("文章不存在");
            }

            // 检查用户是否存在
            User user = userMapper.selectById(comment.getUserId());
            if (user == null) {
                log.error("用户不存在，ID: {}", comment.getUserId());
                throw new RuntimeException("用户不存在");
            }

            // 检查用户是否被禁用
            if (user.getDisabled() != null && user.getDisabled()) {
                log.error("用户已被禁用，不能发表评论，用户ID: {}", comment.getUserId());
                throw new RuntimeException("您的账号已被禁用，暂时无法发表评论");
            }

            // 验证评论内容
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                log.error("评论内容不能为空");
                throw new IllegalArgumentException("评论内容不能为空");
            }

            // 设置时间
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());

            // 保存评论
            log.info("开始保存评论到数据库");
            int rows = commentMapper.insert(comment);
            if (rows != 1) {
                log.error("评论保存失败");
                throw new RuntimeException("评论保存失败");
            }
            log.info("评论保存成功，ID: {}", comment.getCommentId());

            // 创建消息通知文章作者
            if (!post.getUserId().equals(comment.getUserId())) {
                Message message = Message.builder()
                        .userId(post.getUserId())    // 接收消息的用户
                        .postId(comment.getPostId()) // 相关文章
                        .commentId(comment.getCommentId()) // 相关评论
                        .type("comment")
                        .content(String.format("用户 %s 评论了你的文章", user.getUsername()))
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .senderId(user.getUserId())    // 添加发送者ID
                        .senderName(user.getUsername()) // 添加发送者用户名
                        .senderEmail(user.getEmail())   // 添加发送者邮箱
                        .build();
                messageMapper.insertMessage(message);
                log.info("已创建评论通知消息，接收者ID: {}", post.getUserId());
            }

            // 返回完整的评论信息
            Comment createdComment = commentMapper.findByIdWithUser(comment.getCommentId());
            if (createdComment == null) {
                log.error("无法获取创建的评论信息，ID: {}", comment.getCommentId());
                throw new RuntimeException("评论创建失败");
            }
            log.info("评论创建成功，ID: {}", createdComment.getCommentId());
            return createdComment;
        } catch (Exception e) {
            log.error("创建评论失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建评论失败: " + e.getMessage());
        } finally {
            log.info("创建评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Integer postId) {
        log.info("开始获取文章评论，文章ID: {}", postId);
        long startTime = System.currentTimeMillis();
        try {
            return commentMapper.findByPostIdWithUser(postId);
        } finally {
            log.info("获取文章评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, Integer userId) {
        log.info("开始删除评论，ID: {}, 用户ID: {}", commentId, userId);
        long startTime = System.currentTimeMillis();
        try {
            // 检查评论是否存在且属于该用户
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null || !comment.getUserId().equals(userId)) {
                log.error("无权删除此评论，ID: {}", commentId);
                throw new RuntimeException("无权删除此评论");
            }

            commentMapper.deleteById(commentId);
            log.info("评论删除成功，ID: {}", commentId);
        } finally {
            log.info("删除评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByUserId(Integer userId) {
        log.info("开始获取用户评论，用户ID: {}", userId);
        long startTime = System.currentTimeMillis();
        try {
            return commentMapper.findByUserIdWithUserAndPost(userId);
        } finally {
            log.info("获取用户评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        log.info("开始更新评论，ID: {}", comment.getCommentId());
        long startTime = System.currentTimeMillis();
        try {
            // 检查评论是否存在
            Comment existingComment = commentMapper.selectById(comment.getCommentId());
            if (existingComment == null || !existingComment.getUserId().equals(comment.getUserId())) {
                log.error("无权修改此评论，ID: {}", comment.getCommentId());
                throw new RuntimeException("无权修改此评论");
            }

            // 验证评论内容
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("评论内容不能为空");
            }

            // 更新时间
            comment.setUpdatedAt(LocalDateTime.now());

            // 更新评论
            commentMapper.updateById(comment);

            // 返回更新后的评论
            Comment updatedComment = commentMapper.findByIdWithUser(comment.getCommentId());
            log.info("评论更新成功，ID: {}", updatedComment.getCommentId());
            return updatedComment;
        } finally {
            log.info("更新评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByPostIdWithUser(Integer postId) {
        log.info("开始获取带用户信息的文章评论，文章ID: {}", postId);
        long startTime = System.currentTimeMillis();
        try {
            List<Comment> comments = commentMapper.findByPostIdWithUser(postId);
            log.info("获取到{}条评论", comments.size());
            
            // 详细记录每条评论信息
            for (Comment comment : comments) {
                log.info("评论ID: {}, 内容: {}, 用户: {}, 用户名: {}, 头像: {}", 
                    comment.getCommentId(), 
                    comment.getContent(), 
                    comment.getUserId(),
                    comment.getUser() != null ? comment.getUser().getUsername() : "无用户信息",
                    comment.getUser() != null ? comment.getUser().getAvatar() : "无头像");
            }
            
            return comments;
        } finally {
            log.info("获取带用户信息的文章评论完成，耗时: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getAllCommentsWithDetails(String keyword, int offset, int size) {
        return commentMapper.findAllWithDetails(keyword, offset, size);
    }

    @Override
    public int getCommentCount(String keyword) {
        return commentMapper.getCommentCount(keyword);
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setIsDeleted(true);
            commentMapper.updateById(comment);
        }
    }

    @Override
    @Transactional
    public void hideComment(Integer commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setIsHidden(true);
            commentMapper.updateById(comment);
        }
    }

    @Override
    @Transactional
    public void showComment(Integer commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setIsHidden(false);
            commentMapper.updateById(comment);
        }
    }
} 