package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "数据统计接口")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Resource
    private StatisticsService statisticsService;

    @Operation(summary = "获取系统概览数据")
    @GetMapping("/overview")
    public Result<?> getSystemOverview() {
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return Result.success(overview);
    }

    @Operation(summary = "获取用户相关统计")
    @GetMapping("/users")
    public Result<?> getUsersStatistics() {
        Map<String, Object> userStats = statisticsService.getUsersStatistics();
        return Result.success(userStats);
    }

    @Operation(summary = "获取帖子相关统计")
    @GetMapping("/posts")
    public Result<?> getPostsStatistics() {
        Map<String, Object> postStats = statisticsService.getPostsStatistics();
        return Result.success(postStats);
    }

    @Operation(summary = "获取资源相关统计")
    @GetMapping("/resources")
    public Result<?> getResourcesStatistics() {
        Map<String, Object> resourceStats = statisticsService.getResourcesStatistics();
        return Result.success(resourceStats);
    }

    @Operation(summary = "获取所有统计数据")
    @GetMapping("/all")
    public Result<?> getAllStatistics() {
        Map<String, Object> allStats = statisticsService.getAllStatistics();
        return Result.success(allStats);
    }
} 