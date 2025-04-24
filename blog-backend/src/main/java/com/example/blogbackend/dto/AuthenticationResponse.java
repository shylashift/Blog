package com.example.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private Integer userId;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private List<String> roles;
} 