package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.NotificationCreateDTO;
import org.example.springboot.DTO.NotificationUpdateDTO;
import org.example.springboot.entity.Notification;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.NotificationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    @Resource
    private NotificationMapper notificationMapper;

    /**
     * 创建系统通知
     * @param createDTO 创建通知DTO
     * @param userId 发布用户ID
     * @return 创建的通知ID
     */
    public Long createNotification(NotificationCreateDTO createDTO, Long userId) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(createDTO, notification);
        
        notification.setUserId(userId);
        notification.setCreateTime(new Date());
        
        if (notificationMapper.insert(notification) <= 0) {
            throw new ServiceException("通知创建失败");
        }
        
        return notification.getId();
    }

    /**
     * 更新系统通知
     * @param id 通知ID
     * @param updateDTO 更新通知DTO
     */
    public void updateNotification(Long id, NotificationUpdateDTO updateDTO) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }

        if (StringUtils.isNotBlank(updateDTO.getTitle())) {
            notification.setTitle(updateDTO.getTitle());
        }
        
        if (StringUtils.isNotBlank(updateDTO.getContent())) {
            notification.setContent(updateDTO.getContent());
        }
        
        if (updateDTO.getStatus() != null) {
            notification.setStatus(updateDTO.getStatus());
        }
        
        if (notificationMapper.updateById(notification) <= 0) {
            throw new ServiceException("通知更新失败");
        }
    }

    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知信息
     */
    public Notification getNotificationById(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        return notification;
    }

    /**
     * 分页获取通知列表
     * @param title 通知标题（模糊查询）
     * @param status 通知状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<Notification> getNotificationsByPage(String title, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Notification::getTitle, title);
        }
        
        if (status != null) {
            queryWrapper.eq(Notification::getStatus, status);
        }
        
        // 按创建时间降序排列
        queryWrapper.orderByDesc(Notification::getCreateTime);
        
        return notificationMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 获取所有已发布通知
     * @return 通知列表
     */
    public List<Notification> getPublishedNotifications() {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getStatus, 1); // 1-发布状态
        queryWrapper.orderByDesc(Notification::getCreateTime);
        
        return notificationMapper.selectList(queryWrapper);
    }

    /**
     * 删除通知
     * @param id 通知ID
     */
    public void deleteNotification(Long id) {
        if (notificationMapper.deleteById(id) <= 0) {
            throw new ServiceException("通知删除失败");
        }
    }

    /**
     * 批量删除通知
     * @param ids 通知ID列表
     */
    public void batchDeleteNotifications(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("请选择要删除的通知");
        }
        
        int count = notificationMapper.deleteBatchIds(ids);
        if (count <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
    
    /**
     * 更新通知状态
     * @param id 通知ID
     * @param status 状态（0-下线，1-发布）
     */
    public void updateNotificationStatus(Long id, Integer status) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setStatus(status);
        
        if (notificationMapper.updateById(notification) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }
} 