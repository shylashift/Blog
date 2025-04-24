package com.example.blogbackend.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    /**
     * 从token中提取用户名
     * @param token JWT token
     * @return 用户名
     */
    String extractUsername(String token);

    /**
     * 从token中提取指定的claim
     * @param token JWT token
     * @param claimsResolver claim解析函数
     * @return claim值
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * 生成JWT token
     * @param userDetails 用户信息
     * @return JWT token
     */
    String generateToken(UserDetails userDetails);

    /**
     * 生成带有额外信息的JWT token
     * @param extraClaims 额外的claim信息
     * @param userDetails 用户信息
     * @return JWT token
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * 验证token是否有效
     * @param token JWT token
     * @param userDetails 用户信息
     * @return 是否有效
     */
    boolean isTokenValid(String token, UserDetails userDetails);
} 