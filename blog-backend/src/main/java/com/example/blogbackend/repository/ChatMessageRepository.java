package com.example.blogbackend.repository;

import com.example.blogbackend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Integer userId);
    void deleteByUserId(Integer userId);
} 