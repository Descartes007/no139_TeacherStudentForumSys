package org.example.springboot.controller;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.common.Result;
import org.example.springboot.service.CommentLikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论点赞接口")
@RestController
@RequestMapping("/comment/like")
public class CommentLikeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentLikeController.class);
    
    @Resource
    private CommentLikeService commentLikeService;
    
    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/{commentId}")
    public Result<?> toggleLike(@PathVariable Long commentId, HttpServletRequest request) {
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
        
        boolean isLiked = commentLikeService.toggleLike(commentId, userId);
        return Result.success(isLiked ? "点赞成功" : "取消点赞成功", isLiked);
    }
    
    @Operation(summary = "获取评论的点赞数量")
    @GetMapping("/count/{commentId}")
    public Result<?> getLikeCount(@PathVariable Long commentId) {
        int count = commentLikeService.getLikeCount(commentId);
        return Result.success(count);
    }
    
    @Operation(summary = "检查评论是否被当前用户点赞")
    @GetMapping("/check/{commentId}")
    public Result<?> checkLike(@PathVariable Long commentId, HttpServletRequest request) {
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
        
        boolean isLiked = commentLikeService.isLiked(commentId, userId);
        return Result.success(isLiked);
    }
} 