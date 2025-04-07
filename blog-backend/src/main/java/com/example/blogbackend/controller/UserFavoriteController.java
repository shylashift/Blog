package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Favorite;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.FavoriteService;
import com.example.blogbackend.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<List<FavoriteVO>> getUserFavorites(@AuthenticationPrincipal User user) {
        // 检查用户是否为空
        if (user == null) {
            log.error("获取用户收藏列表失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build(); // 返回401未授权状态码
        }
        
        log.info("获取用户收藏列表，用户ID: {}", user.getUserId());
        try {
            List<Favorite> favorites = favoriteService.getUserFavorites(user.getUserId());
            List<FavoriteVO> result = new ArrayList<>();
            
            for (Favorite favorite : favorites) {
                FavoriteVO vo = new FavoriteVO();
                vo.setId(favorite.getFavoriteId());
                vo.setPostId(favorite.getPostId());
                
                // 设置文章和作者信息
                if (favorite.getPost() != null) {
                    vo.setTitle(favorite.getPost().getTitle());
                    // 设置作者名
                    if (favorite.getPost().getUser() != null) {
                        vo.setAuthor(favorite.getPost().getUser().getUsername());
                    } else {
                        vo.setAuthor(favorite.getPost().getAuthorName());
                    }
                }
                
                vo.setCreatedAt(favorite.getCreatedAt());
                result.add(vo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 