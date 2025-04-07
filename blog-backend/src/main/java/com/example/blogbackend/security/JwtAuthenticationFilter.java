package com.example.blogbackend.security;

import com.example.blogbackend.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> publicPaths = Arrays.asList(
        "/auth/**"
    );

    private final List<String> publicGetPaths = Arrays.asList(
        "/posts",
        "/posts/tags",
        "/posts/bytags/**",
        "/posts/*",
        "/tags/**",
        "/users/\\d+" // 只允许获取特定用户ID的公开信息
    );

    private boolean isPublicPath(String path, String method) {
        // 记录当前请求路径
        log.debug("检查路径: {} {}", method, path);
        
        // 总是允许 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("OPTIONS请求，直接放行");
            return true;
        }

        // 检查完全公开的路径
        if (publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
            log.debug("匹配到公开路径: {}", path);
            return true;
        }

        // 检查只允许 GET 的路径
        if ("GET".equalsIgnoreCase(method)) {
            // 特殊处理 /users/me 和 /users/favorites 路径，这些路径需要认证
            if (path.equals("/users/me") || path.equals("/users/favorites")) {
                log.debug("特殊路径需要认证: {}", path);
                return false;
            }
            
            boolean isPublic = publicGetPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
            log.debug("GET请求 {} 是否公开: {}", path, isPublic);
            return isPublic;
        }

        log.debug("非公开路径: {}", path);
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 如果是公开路径，直接放行
        String path = request.getServletPath();
        String method = request.getMethod();
        
        log.info("处理请求: {} {}", method, path);
        
        if (isPublicPath(path, method)) {
            log.info("公开路径，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.info("Authorization头: {}", authHeader != null ? (authHeader.substring(0, Math.min(20, authHeader.length())) + "...") : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少有效的Authorization头");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"未授权访问\"}");
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(jwt);
            log.info("从Token提取用户: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.info("用户 {} 的权限: {}", userEmail, userDetails.getAuthorities());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // 检查是否有管理员权限
                    boolean isAdmin = userDetails.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    log.info("用户 {} 是否为管理员: {}, 所有权限: {}", userEmail, isAdmin, userDetails.getAuthorities());
                    
                    // 特殊处理admin用户，确保其总是有管理员权限
                    if ("admin@example.com".equals(userEmail)) {
                        log.info("检测到admin@example.com用户，强制设置管理员权限");
                        isAdmin = true;
                        // 重新设置认证信息，包含ROLE_ADMIN
                        var authorities = new ArrayList<GrantedAuthority>();
                        authorities.addAll(userDetails.getAuthorities());
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("已更新admin用户的权限: {}", authorities);
                    }
                    
                    // 检查管理员访问
                    if (path.startsWith("/admin/")) {
                        log.info("正在访问管理员资源: {}", path);
                        if (!isAdmin) {
                            log.warn("用户 {} 尝试访问管理员资源: {}, 但权限不足. 当前权限: {}", 
                                    userEmail, path, userDetails.getAuthorities());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"禁止访问管理员资源\"}");
                            return;
                        }
                        log.info("管理员权限验证通过，允许访问");
                    }
                    
                    log.info("认证成功，放行请求: {} {}, 用户: {}, 权限: {}", 
                            method, path, userEmail, userDetails.getAuthorities());
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    log.warn("Token无效");
                }
            } else {
                log.warn("无法提取用户邮箱或已有认证");
            }
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Token无效或已过期\"}");
        } catch (Exception e) {
            log.error("JWT验证失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Token验证失败: " + e.getMessage() + "\"}");
        }
    }
} 