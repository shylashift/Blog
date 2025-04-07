package com.example.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blogbackend.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取用户的未读消息
     * @param userId 用户ID
     * @return 未读消息列表
     */
    @Select("SELECT m.*, p.title as post_title " +
           "FROM messages m " +
           "LEFT JOIN posts p ON m.post_id = p.post_id " +
           "WHERE m.user_id = #{userId} AND m.is_read = false " +
           "ORDER BY m.created_at DESC")
    List<Message> getUnreadMessages(Integer userId);

    /**
     * 获取用户的所有消息
     * @param userId 用户ID
     * @return 消息列表
     */
    @Select("SELECT m.*, p.title as post_title " +
           "FROM messages m " +
           "LEFT JOIN posts p ON m.post_id = p.post_id " +
           "WHERE m.user_id = #{userId} " +
           "ORDER BY m.created_at DESC")
    List<Message> getAllMessages(Integer userId);

    /**
     * 将消息标记为已读
     * @param messageId 消息ID
     * @return 更新的行数
     */
    @Update("UPDATE messages SET is_read = true WHERE message_id = #{messageId}")
    int markMessageAsRead(Integer messageId);

    /**
     * 将用户的所有消息标记为已读
     * @param userId 用户ID
     * @return 更新的行数
     */
    @Update("UPDATE messages SET is_read = true WHERE user_id = #{userId}")
    int markAllMessagesAsRead(Integer userId);

    /**
     * 获取用户的未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM messages WHERE user_id = #{userId} AND is_read = false")
    int getUnreadMessageCount(Integer userId);
} 