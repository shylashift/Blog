package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.mapper.MessageMapper;
import com.example.blogbackend.service.AdminService;
import com.example.blogbackend.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final AdminService adminService;

    @Autowired
    public MessageServiceImpl(MessageMapper messageMapper, AdminService adminService) {
        this.messageMapper = messageMapper;
        this.adminService = adminService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getUserMessages(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取用户消息，用户ID: {}", userId);
        
        try {
            // 使用自定义SQL查询以获取文章标题
            List<Message> messages = messageMapper.getAllMessages(userId);
            log.info("获取到{}条消息", messages.size());
            return messages;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取用户消息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public void markAsRead(Integer messageId, Integer userId) {
        Instant start = Instant.now();
        log.info("开始标记消息为已读，消息ID: {}", messageId);
        
        try {
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                throw new RuntimeException("消息不存在");
            }
            
            if (!message.getUserId().equals(userId)) {
                throw new RuntimeException("无权操作此消息");
            }
            
            messageMapper.markMessageAsRead(messageId);
            log.info("消息已标记为已读");
        } catch (Exception e) {
            log.error("标记消息为已读时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("标记消息为已读完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        Instant start = Instant.now();
        log.info("开始标记所有消息为已读，用户ID: {}", userId);
        
        try {
            messageMapper.markAllMessagesAsRead(userId);
            log.info("已标记所有消息为已读");
        } catch (Exception e) {
            log.error("标记所有消息为已读时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("标记所有消息为已读完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCount(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取未读消息数量，用户ID: {}", userId);
        
        try {
            return messageMapper.getUnreadMessageCount(userId);
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取未读消息数量完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public void deleteMessage(Integer messageId, Integer userId) {
        Instant start = Instant.now();
        log.info("开始删除消息，消息ID: {}", messageId);
        
        try {
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                throw new RuntimeException("消息不存在");
            }
            
            if (!message.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除此消息");
            }
            
            messageMapper.deleteById(messageId);
            log.info("消息删除成功");
        } catch (Exception e) {
            log.error("删除消息时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("删除消息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        Instant start = Instant.now();
        log.info("开始创建消息，接收者ID: {}", message.getUserId());
        
        try {
            message.setCreatedAt(LocalDateTime.now());
            message.setIsRead(false);
            messageMapper.insertMessage(message);
            log.info("消息创建成功，ID: {}", message.getMessageId());
            return message;
        } catch (Exception e) {
            log.error("创建消息时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("创建消息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public Message sendMessageToAdmin(Integer senderId, Integer adminId, String content,
                                    String senderName, String senderEmail) {
        // 验证接收者是否为管理员
        if (!adminService.isAdmin(adminId)) {
            throw new IllegalArgumentException("接收者不是管理员");
        }

        Message message = Message.builder()
                .senderId(senderId)
                .userId(adminId)  // 接收者ID
                .type(Message.TYPE_ADMIN_MESSAGE)
                .content(content)
                .senderName(senderName)
                .senderEmail(senderEmail)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        messageMapper.insertMessage(message);
        return message;
    }
} 