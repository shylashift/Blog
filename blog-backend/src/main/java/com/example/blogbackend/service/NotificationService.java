package com.example.blogbackend.service;

import com.example.blogbackend.entity.Message   ;
import java.util.List;

public interface NotificationService {
    List<Message> getUserNotifications(Integer userId);
    void markAsRead(Integer messageId, Integer userId);
    Integer getUnreadCount(Integer userId);
} 