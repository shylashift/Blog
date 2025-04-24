package com.example.blogbackend.controller;

import com.example.blogbackend.entity.ChatMessage;
import com.example.blogbackend.service.ChatMessageService;
import com.example.blogbackend.util.SecurityUtils;
import com.example.blogbackend.model.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class ChatController {
    private final ChatMessageService chatMessageService;

    @Autowired
    public ChatController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request) {
        Integer userId = SecurityUtils.getCurrentUserId();
        String content = request.getContent();
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("消息内容不能为空");
        }

        try {
            Long chatId = request.getChatId();
            ChatMessage response = chatMessageService.sendMessage(userId, content, chatId != null ? chatId.intValue() : null);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("messageId", response.getChatId().toString());
            responseMap.put("chatId", response.getChatId());
            responseMap.put("content", response.getContent());
            responseMap.put("createdAt", response.getCreatedAt().toString());
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("发送消息失败");
        }
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<Map<String, Object>>> getChatHistory() {
        Integer userId = SecurityUtils.getCurrentUserId();
        try {
            System.out.println("正在获取用户ID为" + userId + "的聊天历史");
            List<ChatMessage> history = chatMessageService.getChatHistory(userId);
            System.out.println("用户" + userId + "的聊天历史记录条数: " + history.size());
            
            List<Map<String, Object>> formattedHistory = history.stream()
                .map(msg -> {
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("id", msg.getChatId().toString());
                    messageMap.put("chatId", msg.getChatId());
                    messageMap.put("userId", msg.getUserId());
                    messageMap.put("role", msg.getRole());
                    messageMap.put("content", msg.getContent());
                    messageMap.put("createdAt", msg.getCreatedAt().toString());
                    return messageMap;
                })
                .collect(Collectors.toList());
            
            System.out.println("格式化后的聊天历史记录: " + formattedHistory);
            return ResponseEntity.ok(formattedHistory);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("获取聊天历史时发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/chat/history")
    public ResponseEntity<?> clearHistory() {
        Integer userId = SecurityUtils.getCurrentUserId();
        try {
            chatMessageService.clearHistory(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("清空历史记录失败");
        }
    }
}