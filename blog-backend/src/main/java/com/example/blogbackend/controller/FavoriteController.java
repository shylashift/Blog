package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Favorite;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{postId}")
    public ResponseEntity<Favorite> addFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        Favorite favorite = favoriteService.addFavorite(user.getUserId(), postId);
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        favoriteService.removeFavorite(user.getUserId(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable Integer userId) {
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<Boolean> checkFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Integer postId
    ) {
        boolean isFavorited = favoriteService.checkFavorite(user.getUserId(), postId);
        return ResponseEntity.ok(isFavorited);
    }
} 