package com.example.blogbackend.config;

import com.example.blogbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.http.HttpMethod;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .authorizeRequests()
            // Swagger UI相关路径
            .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
            // 公开访问的接口
            .antMatchers("/api/auth/**", "/auth/**").permitAll()
            .antMatchers("/api/uploads/**", "/uploads/**").permitAll()  // 允许直接访问上传的文件
            // 允许所有GET请求访问这些API
            .antMatchers(HttpMethod.GET, "/api/posts/**", "/posts/**", "/api/tags/**", "/tags/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/users/[0-9]+", "/users/[0-9]+").permitAll() // 仅允许查看特定用户ID的资料
            // 允许所有OPTIONS请求
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 通知相关接口需要认证
            .antMatchers("/api/notifications/**", "/notifications/**").authenticated()
            // 管理员专用接口
            .antMatchers("/api/admin/**", "/admin/**").hasAuthority("管理员")
            // 其他所有请求需要认证
            .anyRequest().authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"未授权访问\"}");
            });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:8080",
            "http://127.0.0.1:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "Accept",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Origin",
            "X-Requested-With"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 