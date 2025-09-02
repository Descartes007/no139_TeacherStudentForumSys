package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Notification;
import org.example.springboot.DTO.NotificationCreateDTO;
import org.example.springboot.DTO.NotificationUpdateDTO;
import org.example.springboot.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="系统通知接口")
@RestController
@RequestMapping("/notification")
public class NotificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    @Resource
    private NotificationService notificationService;

    @Operation(summary = "根据id获取通知信息")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return Result.success(notification);
    }

    @Operation(summary = "获取所有已发布通知")
    @GetMapping("/published")
    public Result<?> getPublishedNotifications() {
        List<Notification> notifications = notificationService.getPublishedNotifications();
        return Result.success(notifications);
    }

    @Operation(summary = "分页查询通知")
    @GetMapping("/page")
    public Result<?> getNotificationsByPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Notification> page = notificationService.getNotificationsByPage(title, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "创建通知")
    @PostMapping("/add")
    public Result<?> createNotification(@RequestBody @Valid NotificationCreateDTO createDTO) {
        // 替换为实际从登录用户中获取的userId
        Long userId = 1L; // 这里假设为管理员ID
        Long notificationId = notificationService.createNotification(createDTO, userId);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新通知信息")
    @PutMapping("/{id}")
    public Result<?> updateNotification(@PathVariable Long id, @RequestBody NotificationUpdateDTO updateDTO) {
        notificationService.updateNotification(id, updateDTO);
        return Result.success("更新成功");
    }

    @Operation(summary = "更新通知状态")
    @PutMapping("/{id}/status")
    public Result<?> updateNotificationStatus(@PathVariable Long id, @RequestParam Integer status) {
        notificationService.updateNotificationStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "根据id删除通知")
    @DeleteMapping("/{id}")
    public Result<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success("删除成功");
    }
    
    @Operation(summary = "批量删除通知")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteNotifications(@RequestBody List<Long> ids) {
        notificationService.batchDeleteNotifications(ids);
        return Result.success("批量删除成功");
    }
} 