package com.example.blogbackend.controller;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.MessageService;
import com.example.blogbackend.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    @GetMapping("/all")
    public ResponseEntity<List<Message>> getAllMessages(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("获取所有消息失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        List<Message> messages = messageService.getAllMessages(user.getUserId());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/discussions")
    public ResponseEntity<List<MessageVO>> getDiscussionMessages(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.error("获取学术讨论消息失败: 用户未登录或会话已过期");
            return ResponseEntity.status(401).build();
        }
        
        log.info("获取学术讨论消息，用户ID: {}", user.getUserId());
        try {
            List<Message> messages = messageService.getAllMessages(user.getUserId());
            List<MessageVO> result = new ArrayList<>();
            
            if (messages.isEmpty()) {
                log.info("用户没有消息");
                return ResponseEntity.ok(result);
            }
            
            for (Message message : messages) {
                MessageVO vo = new MessageVO();
                vo.setId(message.getMessageId());
                vo.setType(message.getType() != null ? message.getType() : "unknown");
                vo.setPostId(message.getPostId());
                vo.setCreatedAt(message.getCreatedAt());
                vo.setRead(message.getIsRead() != null ? message.getIsRead() : false);
                
                // 设置消息内容
                vo.setTitle(message.getPostTitle() != null ? message.getPostTitle() : "未知文章");
                // 我们没有发送者名称，这里设置一个默认值
                vo.setAuthor("系统通知");
                
                // 根据消息类型设置内容
                if ("comment".equals(message.getType())) {
                    vo.setContent("有人评论了您的文章");
                } else if ("favorite".equals(message.getType())) {
                    vo.setContent("有人收藏了您的文章");
                } else {
                    vo.setContent("新消息");
                }
                
                result.add(vo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取学术讨论消息失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
} 