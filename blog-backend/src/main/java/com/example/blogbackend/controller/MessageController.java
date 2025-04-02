package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Message>> getUnreadMessages(@AuthenticationPrincipal User user) {
        List<Message> messages = messageService.getUnreadMessages(user.getUserId());
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Integer messageId
    ) {
        messageService.markMessageAsRead(messageId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllMessagesAsRead(@AuthenticationPrincipal User user) {
        messageService.markAllMessagesAsRead(user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUnreadMessageCount(@AuthenticationPrincipal User user) {
        int count = messageService.getUnreadMessageCount(user.getUserId());
        return ResponseEntity.ok(count);
    }
} 