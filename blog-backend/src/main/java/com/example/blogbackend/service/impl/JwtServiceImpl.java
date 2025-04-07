package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public String extractUsername(String token) {
        Instant start = Instant.now();
        log.debug("开始从token中提取用户名");
        
        try {
            return extractClaim(token, Claims::getSubject);
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.debug("提取用户名完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Instant start = Instant.now();
        log.info("开始生成JWT token，用户: {}", ((User) userDetails).getEmail());
        
        try {
            User user = (User) userDetails;
            Map<String, Object> claims = new HashMap<>(extraClaims);
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList()));
            
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
            log.info("JWT token生成成功，包含角色: {}", userDetails.getAuthorities());
            return token;
        } catch (Exception e) {
            log.error("生成JWT token时发生错误", e);
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.info("生成JWT token完成，耗时: {}ms", duration.toMillis());
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Instant start = Instant.now();
        log.debug("开始验证token");
        
        try {
            final String email = extractUsername(token);
            boolean isValid = (email.equals(((User) userDetails).getEmail())) && !isTokenExpired(token);
            log.debug("Token验证结果: {}", isValid);
            return isValid;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            log.debug("验证token完成，耗时: {}ms", duration.toMillis());
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析token时发生错误", e);
            throw e;
        }
    }
} 