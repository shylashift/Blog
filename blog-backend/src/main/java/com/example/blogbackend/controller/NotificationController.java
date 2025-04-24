package com.example.blogbackend.controller;

import com.example.blogbackend.service.NotificationService;
import com.example.blogbackend.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.blogbackend.entity.User; // 确保路径正确
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity<List<Message>> getUserNotifications(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Message> notifications = notificationService.getUserNotifications(user.getUserId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer messageId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAsRead(messageId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer count = notificationService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(count);
    }
} 