package com.example.blogbackend.service;

import com.example.blogbackend.entity.ChatMessage;
import com.example.blogbackend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final DeepSeekService deepSeekService;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository, DeepSeekService deepSeekService) {
        this.chatMessageRepository = chatMessageRepository;
        this.deepSeekService = deepSeekService;
    }

    public List<ChatMessage> getChatHistory(Integer userId) {
        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public ChatMessage sendMessage(Integer userId, String content) {
        return sendMessage(userId, content, null);
    }

    @Transactional
    public ChatMessage sendMessage(Integer userId, String content, Integer chatId) {
        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setCreatedAt(LocalDateTime.now());
        if (chatId != null) {
            userMessage.setChatId(chatId);
        }
        chatMessageRepository.save(userMessage);

        // 获取AI回复
        String aiResponse = deepSeekService.generateResponse(content);

        // 保存AI回复
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiMessage.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(aiMessage);

        return aiMessage;
    }

    @Transactional
    public void clearHistory(Integer userId) {
        chatMessageRepository.deleteByUserId(userId);
    }
} 