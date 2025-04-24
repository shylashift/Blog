package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Message>> getUserMessages(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Message> messages = messageService.getUserMessages(user.getUserId());
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Integer messageId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        messageService.markAsRead(messageId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        messageService.markAllAsRead(user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer count = messageService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Integer messageId
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        try {
            messageService.deleteMessage(messageId, user.getUserId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除消息失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> sendMessageToAdmin(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Object> request
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "请先登录"));
        }

        try {
            Integer recipientId = (Integer) request.get("recipientId");
            String content = (String) request.get("content");
            @SuppressWarnings("unchecked")
            Map<String, String> senderInfo = (Map<String, String>) request.get("senderInfo");

            if (recipientId == null || content == null || senderInfo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "缺少必要参数"));
            }

            Message message = messageService.sendMessageToAdmin(
                currentUser.getUserId(),
                recipientId,
                content,
                senderInfo.get("username"),
                senderInfo.get("email")
            );

            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "发送消息失败"));
        }
    }
} 