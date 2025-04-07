package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.Message;
import com.example.blogbackend.mapper.MessageMapper;
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

    @Autowired
    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取未读消息，用户ID: {}", userId);
        
        try {
            List<Message> messages = messageMapper.getUnreadMessages(userId);
            log.info("获取到{}条未读消息", messages.size());
            return messages;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取未读消息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getAllMessages(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取所有消息，用户ID: {}", userId);
        
        try {
            List<Message> messages = messageMapper.getAllMessages(userId);
            log.info("获取到{}条消息", messages.size());
            return messages;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("获取所有消息完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    @Transactional
    public void markMessageAsRead(Integer messageId, Integer userId) {
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
            
            int result = messageMapper.markMessageAsRead(messageId);
            if (result != 1) {
                throw new RuntimeException("标记消息为已读失败");
            }
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
    public void markAllMessagesAsRead(Integer userId) {
        Instant start = Instant.now();
        log.info("开始标记所有消息为已读，用户ID: {}", userId);
        
        try {
            int result = messageMapper.markAllMessagesAsRead(userId);
            log.info("已标记{}条消息为已读", result);
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
    public int getUnreadMessageCount(Integer userId) {
        Instant start = Instant.now();
        log.info("开始获取未读消息数量，用户ID: {}", userId);
        
        try {
            int count = messageMapper.getUnreadMessageCount(userId);
            log.info("未读消息数量: {}", count);
            return count;
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
            
            int result = messageMapper.deleteById(messageId);
            if (result != 1) {
                throw new RuntimeException("删除消息失败");
            }
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
            
            int result = messageMapper.insert(message);
            if (result != 1) {
                throw new RuntimeException("创建消息失败");
            }
            
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
} 