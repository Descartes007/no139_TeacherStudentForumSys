package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PostFavorite;
import org.example.springboot.service.PostFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

@Tag(name = "帖子收藏接口")
@RestController
@RequestMapping("/post/favorite")
public class PostFavoriteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostFavoriteController.class);
    
    @Resource
    private PostFavoriteService postFavoriteService;
    
    @Operation(summary = "收藏/取消收藏帖子")
    @PostMapping("/{postId}")
    public Result<?> toggleFavorite(@PathVariable Long postId, HttpServletRequest request) {
        // 从请求头中获取token并解析用户ID
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            return Result.error("未授权，请先登录");
        }
        
        Long userId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Long.valueOf(userIdStr);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
        
        boolean isFavorite = postFavoriteService.toggleFavorite(postId, userId);
        return Result.success(isFavorite ? "收藏成功" : "取消收藏成功", isFavorite);
    }

    
    @Operation(summary = "查询帖子是否被当前用户收藏")
    @GetMapping("/check/{postId}")
    public Result<?> checkFavorite(@PathVariable Long postId, HttpServletRequest request) {
        // 从请求头中获取token并解析用户ID
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            return Result.error("未授权，请先登录");
        }
        
        Long userId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Long.valueOf(userIdStr);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
        
        boolean isFavorite = postFavoriteService.isFavorited(postId, userId);
        return Result.success(isFavorite);
    }
    
    @Operation(summary = "获取帖子收藏数量")
    @GetMapping("/count/{postId}")
    public Result<?> getFavoriteCount(@PathVariable Long postId) {
        int count = postFavoriteService.getFavoriteCount(postId);
        return Result.success(count);
    }
    
    @Operation(summary = "获取用户收藏的帖子列表")
    @GetMapping("/user")
    public Result<?> getUserFavorites(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 从请求头中获取token并解析用户ID
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            return Result.error("未授权，请先登录");
        }
        
        Long userId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Long.valueOf(userIdStr);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
        
        Page<PostFavorite> favorites = postFavoriteService.getUserFavorites(userId, currentPage, size);
        return Result.success(favorites);
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{favoriteId}")
    public Result<?> cancelFavorite(@PathVariable Long favoriteId, HttpServletRequest request) {
        // 从请求头中获取token并解析用户ID
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            return Result.error("未授权，请先登录");
        }
        
        Long userId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            userId = Long.valueOf(userIdStr);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
        
        postFavoriteService.cancelFavorite(favoriteId, userId);
        return Result.success("取消收藏成功");
    }
} 