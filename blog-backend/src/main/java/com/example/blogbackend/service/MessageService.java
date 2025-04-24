package com.example.blogbackend.service;

import com.example.blogbackend.entity.Message;
import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 获取用户的所有消息
     * @param userId 用户ID
     * @return 消息列表
     */
    List<Message> getUserMessages(Integer userId);

    /**
     * 将消息标记为已读
     * @param messageId 消息ID
     * @param userId 用户ID（用于权限验证）
     */
    void markAsRead(Integer messageId, Integer userId);

    /**
     * 将用户的所有消息标记为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Integer userId);

    /**
     * 获取用户的未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadCount(Integer userId);

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

    /**
     * 发送消息给管理员
     * @param senderId 发送者ID
     * @param adminId 管理员ID
     * @param content 消息内容
     * @param senderName 发送者用户名
     * @param senderEmail 发送者邮箱
     * @return 创建的消息
     */
    Message sendMessageToAdmin(Integer senderId, Integer adminId, String content, 
                             String senderName, String senderEmail);
} 