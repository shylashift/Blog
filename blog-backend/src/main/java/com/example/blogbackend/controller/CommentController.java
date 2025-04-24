package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Comment;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody Comment comment
    ) {
        comment.setUserId(user.getUserId());
        Comment createdComment = commentService.createComment(comment);
        return ResponseEntity.ok(createdComment);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Integer postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Comment>> getUserComments(@AuthenticationPrincipal User user) {
        List<Comment> comments = commentService.getCommentsByUserId(user.getUserId());
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User user,
            @PathVariable Integer commentId
    ) {
        commentService.deleteComment(commentId, user.getUserId());
        return ResponseEntity.ok().build();
    }
} 