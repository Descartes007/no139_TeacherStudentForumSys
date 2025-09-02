package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ResourceDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ForumResource;
import org.example.springboot.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.List;

@Tag(name = "资源管理接口")
@RestController
@RequestMapping("/resource")
public class ResourceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);
    
    @Resource
    private ResourceService resourceService;

    @Operation(summary = "创建资源")
    @PostMapping("/add")
    public Result<?> createResource(@RequestBody @Valid ResourceDTO resourceDTO, HttpServletRequest request) {
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
        
        resourceService.createResource(resourceDTO, userId);
        return Result.success("资源创建成功");
    }

    @Operation(summary = "更新资源信息")
    @PutMapping("/{id}")
    public Result<?> updateResource(@PathVariable Long id, @RequestBody ResourceDTO resourceDTO) {
        resourceService.updateResource(id, resourceDTO);
        return Result.success("资源更新成功");
    }

    @Operation(summary = "获取所有资源")
    @GetMapping("/all")
    public Result<?> getResourceList() {
        List<ForumResource> resources = resourceService.getResourceList();
        return Result.success(resources);
    }

    @Operation(summary = "分页查询资源")
    @GetMapping("/page")
    public Result<?> getResourcesByPage(
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ForumResource> page = resourceService.getResourcesByPage(resourceName, categoryId, fileType, userId, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取资源")
    @GetMapping("/{id}")
    public Result<?> getResourceById(@PathVariable Long id) {
        ForumResource resource = resourceService.getResourceById(id);
        return Result.success(resource);
    }

    @Operation(summary = "根据用户ID获取资源列表")
    @GetMapping("/user/{userId}")
    public Result<?> getResourcesByUserId(@PathVariable Long userId) {
        List<ForumResource> resources = resourceService.getResourcesByUserId(userId);
        return Result.success(resources);
    }

    @Operation(summary = "根据分类ID获取资源列表")
    @GetMapping("/getByCategory/{categoryId}")
    public Result<?> getResourcesByCategoryId(@PathVariable Long categoryId) {
        List<ForumResource> resources = resourceService.getResourcesByCategoryId(categoryId);
        return Result.success(resources);
    }

    @Operation(summary = "根据ID删除资源")
    @DeleteMapping("/{id}")
    public Result<?> deleteResource(@PathVariable Long id) {
        resourceService.deleteResourceById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除资源")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        resourceService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "更新资源状态")
    @PutMapping("/{id}/status")
    public Result<?> updateResourceStatus(@PathVariable Long id, @RequestParam Integer status) {
        resourceService.updateResourceStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "增加资源下载次数")
    @PutMapping("/{id}/download")
    public Result<?> incrementDownloadCount(@PathVariable Long id) {
        resourceService.incrementDownloadCount(id);
        return Result.success("下载次数更新成功");
    }
} 