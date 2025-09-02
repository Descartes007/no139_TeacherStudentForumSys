package org.example.springboot.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security安全配置类
 * 用于配置系统的安全认证、授权等功能
 * 包括：
 * - 密码加密方式
 * - 安全过滤器链
 * - 请求授权规则
 * - CSRF防护配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Resource
    private UserDetailsService userDetailsService;

    /**
     * 密码编码器配置
     * 使用BCrypt加密算法对密码进行加密
     * BCrypt是一种安全的密码哈希函数，自动包含随机盐值
     *
     * @return PasswordEncoder BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // 设置加密强度
    }

    /**
     * 安全过滤器链配置
     * 配置系统的安全规则，包括：
     * 1. 请求授权规则
     *    - 登录注册路径允许匿名访问
     *    - 其他请求交由JWT拦截器处理
     * 2. CSRF防护已禁用（RESTful API通常不需要）
     *
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 配置好的安全过滤器链
     * @throws Exception 配置过程中可能发生的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/login", "/api/user/register", "/api/user/add", "/api/user/forget").permitAll()  // 登录注册相关接口无需认证
                        .requestMatchers("/api/v3/api-docs/**", "/api/swagger-ui/**", "/api/doc.html", "/api/webjars/**").permitAll() // Swagger文档相关接口
                        .requestMatchers("/api/img/**", "/api/file/**").permitAll() // 静态资源
                        .anyRequest().permitAll()  // 其他请求由JWT拦截器处理，这里先放行
                )
                .csrf().disable()  // 禁用 CSRF 保护（RESTful API不需要）
                .cors();  // 启用CORS支持

        return http.build();
    }
}