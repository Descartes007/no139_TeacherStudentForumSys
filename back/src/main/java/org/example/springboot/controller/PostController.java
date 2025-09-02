package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.springboot.DTO.PostDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Post;
import org.example.springboot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.List;
import java.util.Map;

@Tag(name = "帖子管理接口")
@RestController
@RequestMapping("/post")
public class PostController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);
    
    @Resource
    private PostService postService;

    @Operation(summary = "创建帖子")
    @PostMapping("/add")
    public Result<?> createPost(@RequestBody @Valid PostDTO postDTO, HttpServletRequest request) {
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
        
        postService.createPost(postDTO, userId);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新帖子信息")
    @PutMapping("/{id}")
    public Result<?> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        postService.updatePost(id, postDTO);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取所有帖子")
    @GetMapping("/all")
    public Result<?> getPostList() {
        List<Post> posts = postService.getPostList();
        return Result.success(posts);
    }

    @Operation(summary = "分页查询帖子")
    @GetMapping("/page")
    public Result<?> getPostsByPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Integer isEssence,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> page = postService.getPostsByPage(title, userId, sectionId, isEssence, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取帖子")
    @GetMapping("/{id}")
    public Result<?> getPostById(@PathVariable Long id, HttpServletRequest request) {
        // 尝试获取当前用户ID（如果已登录）
        Long userId = null;
        String token = request.getHeader("token");
        if (StringUtils.isNotBlank(token)) {
            try {
                userId = Long.valueOf(JWT.decode(token).getAudience().get(0));
            } catch (Exception e) {
                // 忽略解析错误，userId保持为null
            }
        }
        
        // 获取帖子详情，包含收藏状态
        Post post = postService.getPostDetailById(id, userId);
        return Result.success(post);
    }

    @Operation(summary = "根据用户ID获取帖子列表")
    @GetMapping("/user/{userId}")
    public Result<?> getPostsByUserId(@PathVariable Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
        return Result.success(posts);
    }

    @Operation(summary = "根据版区ID获取帖子列表")
    @GetMapping("/section/{sectionId}")
    public Result<?> getPostsBySectionId(@PathVariable Long sectionId) {
        List<Post> posts = postService.getPostsBySectionId(sectionId);
        return Result.success(posts);
    }

    @Operation(summary = "根据ID删除帖子")
    @DeleteMapping("/{id}")
    public Result<?> deletePost(@PathVariable Long id) {
        postService.deletePostById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除帖子")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        postService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "更新帖子状态")
    @PutMapping("/{id}/status")
    public Result<?> updatePostStatus(@PathVariable Long id, @RequestParam Integer status) {
        postService.updatePostStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "设置/取消精华帖")
    @PutMapping("/{id}/essence")
    public Result<?> updatePostEssence(@PathVariable Long id, @RequestParam Integer isEssence) {
        postService.updatePostEssence(id, isEssence);
        return Result.success("精华状态更新成功");
    }

    @Operation(summary = "增加帖子浏览次数")
    @PutMapping("/{id}/view")
    public Result<?> incrementViewCount(@PathVariable Long id) {
        postService.incrementViewCount(id);
        return Result.success("浏览次数更新成功");
    }

    @Operation(summary = "分页获取最热贴子")
    @GetMapping("/hot")
    public Result<?> getHotPosts() {
        Page<Post> posts = postService.getPostByViewCount();
        return Result.success(posts);
    }

    @Operation(summary = "获取板块帖子统计信息")
    @GetMapping("/section/stats/{sectionId}")
    public Result<?> getSectionPostStats(@PathVariable Long sectionId) {
        Map<String, Object> stats = postService.getSectionPostStats(sectionId);
        return Result.success(stats);
    }
} 