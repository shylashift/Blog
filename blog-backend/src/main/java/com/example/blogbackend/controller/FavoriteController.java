package com.example.blogbackend.controller;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/posts/{postId}/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<Boolean> checkFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        log.info("检查收藏状态，用户ID: {}, 文章ID: {}", user != null ? user.getUserId() : "未登录", postId);
        if (user == null) {
            return ResponseEntity.ok(false);
        }
        boolean isFavorited = favoriteService.checkFavorite(user.getUserId(), postId);
        log.info("收藏状态: {}", isFavorited);
        return ResponseEntity.ok(isFavorited);
    }

    @PostMapping
    public ResponseEntity<Void> addFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        log.info("添加收藏，用户ID: {}, 文章ID: {}", user.getUserId(), postId);
        favoriteService.addFavorite(user.getUserId(), postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        log.info("取消收藏，用户ID: {}, 文章ID: {}", user.getUserId(), postId);
        favoriteService.removeFavorite(user.getUserId(), postId);
        return ResponseEntity.ok().build();
    }
} 