package com.example.blogbackend.service;

import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.Post;
import com.example.blogbackend.repository.CommentRepository;
import com.example.blogbackend.repository.MessageRepository;
import com.example.blogbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Comment createComment(Comment comment) {
        // 检查文章是否存在
        Post post = postRepository.selectById(comment.getPostId());
        if (post == null) {
            throw new RuntimeException("文章不存在");
        }

        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.insert(comment);

        // 创建消息通知文章作者
        if (!post.getUserId().equals(comment.getUserId())) {
            Message message = Message.builder()
                    .userId(post.getUserId())
                    .postId(post.getPostId())
                    .commentId(comment.getCommentId())
                    .type("comment")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.insert(message);
        }

        return commentRepository.selectById(comment.getCommentId());
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Integer postId) {
        return commentRepository.findByPostIdWithUser(postId);
    }

    @Transactional
    public void deleteComment(Integer commentId, Integer userId) {
        Comment comment = commentRepository.selectById(commentId);
        if (comment == null || !comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        commentRepository.deleteById(commentId);
    }
} 