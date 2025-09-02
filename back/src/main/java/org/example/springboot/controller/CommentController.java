package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.springboot.DTO.CommentDTO;
import org.example.springboot.DTO.CommentTreeDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Comment;
import org.example.springboot.service.CommentService;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理", description = "评论的增删改查")
public class CommentController {
    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    @Operation(summary = "创建评论", description = "创建新的评论")
    public Result createComment(@RequestBody @Valid CommentDTO commentDTO, HttpServletRequest request) {
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
        
        commentService.createComment(commentDTO, userId);
        return Result.success("评论创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评论", description = "根据ID更新评论信息")
    public Result updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        commentService.updateComment(id, commentDTO);
        return Result.success("评论更新成功");
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有评论", description = "获取所有评论列表")
    public Result getCommentList() {
        List<Comment> commentList = commentService.getCommentList();
        return Result.success(commentList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询评论", description = "分页查询评论，支持多条件筛选")
    public Result getCommentsByPage(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String postTitle,
            @RequestParam(required = false) Integer postId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> page = commentService.getCommentsByPage(content, username, postTitle, postId,status, current, size);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取评论", description = "根据ID获取评论详细信息")
    public Result getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return Result.success(comment);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "获取帖子的评论", description = "获取指定帖子的所有评论")
    public Result getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return Result.success(comments);
    }

    @Operation(summary = "获取帖子的评论树")
    @GetMapping("/tree/{postId}")
    public Result<?> getCommentTree(@PathVariable Long postId, HttpServletRequest request) {
        // 尝试获取当前用户ID
        Long userId = null;
        String token = request.getHeader("token");
        if (StringUtils.isNotBlank(token)) {
            try {
                userId = Long.valueOf(JWT.decode(token).getAudience().get(0));
            } catch (Exception e) {
                // 用户未登录，不影响评论查询
            }
        }
        
        List<CommentTreeDTO> commentTree = commentService.getCommentTreeByPostIdWithLikeInfo(postId, userId);
        return Result.success(commentTree);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的评论", description = "获取指定用户的所有评论")
    public Result getCommentsByUserId(@PathVariable Long userId) {
        List<Comment> comments = commentService.getCommentsByUserId(userId);
        return Result.success(comments);
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "获取子评论", description = "获取指定父评论下的所有子评论")
    public Result getCommentsByParentId(@PathVariable Long parentId) {
        List<Comment> comments = commentService.getCommentsByParentId(parentId);
        return Result.success(comments);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "根据ID删除评论")
    public Result deleteComment(@PathVariable Long id) {
        commentService.deleteCommentById(id);
        return Result.success("评论删除成功");
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "用户删除自己的评论", description = "用户删除自己的评论")
    public Result deleteUserComment(@PathVariable Long id, HttpServletRequest request) {
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
        
        // 调用服务删除用户自己的评论
        boolean success = commentService.deleteUserComment(id, userId);
        if (success) {
            return Result.success("评论删除成功");
        } else {
            return Result.error("您只能删除自己的评论");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除评论", description = "批量删除评论")
    public Result deleteBatch(@RequestParam List<Integer> ids) {
        commentService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新评论状态", description = "更新评论的状态（0-删除，1-正常）")
    public Result updateCommentStatus(@PathVariable Long id, @RequestParam Integer status) {
        commentService.updateCommentStatus(id, status);
        return Result.success("状态更新成功");
    }
} 