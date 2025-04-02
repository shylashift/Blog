package com.example.blogbackend.service;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(Integer userId) {
        return messageRepository.findUnreadByUserId(userId);
    }

    @Transactional
    public void markMessageAsRead(Integer messageId, Integer userId) {
        Message message = messageRepository.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此消息");
        }
        messageRepository.markAsRead(messageId);
    }

    @Transactional
    public void markAllMessagesAsRead(Integer userId) {
        List<Message> unreadMessages = messageRepository.findUnreadByUserId(userId);
        for (Message message : unreadMessages) {
            messageRepository.markAsRead(message.getMessageId());
        }
    }

    @Transactional(readOnly = true)
    public int getUnreadMessageCount(Integer userId) {
        List<Message> unreadMessages = messageRepository.findUnreadByUserId(userId);
        return unreadMessages.size();
    }
} 