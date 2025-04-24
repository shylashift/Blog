package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.blogbackend.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> getUserNotifications(Integer userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("UserId", userId)
               .orderByDesc("CreatedAt");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public void markAsRead(Integer messageId, Integer userId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setIsRead(true);
        
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("MessageId", messageId)
               .eq("UserId", userId);
        
        messageMapper.update(message, wrapper);
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("UserId", userId)
               .eq("IsRead", false);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }
} 