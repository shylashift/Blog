package com.example.blogbackend.controller;

import com.example.blogbackend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Integer userId) {
        log.info("收到提升用户权限请求: userId={}", userId);
        adminService.promoteToAdmin(userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<Void> demoteFromAdmin(@PathVariable Integer userId) {
        log.info("收到降低用户权限请求: userId={}", userId);
        adminService.demoteFromAdmin(userId);
        return ResponseEntity.ok().build();
    }
} 