package com.example.blogbackend.controller;

import com.example.blogbackend.dto.AuthenticationRequest;
import com.example.blogbackend.dto.AuthenticationResponse;
import com.example.blogbackend.dto.RegisterRequest;
import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("收到注册请求: {}", request);
        try {
            AuthenticationResponse response = authenticationService.register(request);
            log.info("注册成功: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        log.info("收到登录请求: {}", request);
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            log.info("登录成功: {}, 角色: {}", response.getUsername(), response.getRoles());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@AuthenticationPrincipal User user) {
        log.info("收到token验证请求，用户: {}", user != null ? user.getEmail() : "未认证");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }
} 