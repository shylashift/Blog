package com.example.blogbackend.service;

import com.example.blogbackend.entity.Message;
import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 获取用户的未读消息
     * @param userId 用户ID
     * @return 未读消息列表
     */
    List<Message> getUnreadMessages(Integer userId);

    /**
     * 获取用户的所有消息
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> getAllMessages(Integer userId);

    /**
     * 将消息标记为已读
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     */
    void markMessageAsRead(Integer messageId, Integer userId);

    /**
     * 将用户的所有消息标记为已读
     * @param userId 用户ID
     */
    void markAllMessagesAsRead(Integer userId);

    /**
     * 获取用户的未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Integer userId);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteMessage(Integer messageId, Integer userId);

    /**
     * 创建新消息
     * @param message 消息信息
     * @return 创建的消息
     */
    Message createMessage(Message message);
} 