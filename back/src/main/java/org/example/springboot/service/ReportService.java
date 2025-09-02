package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ReportDTO;
import org.example.springboot.entity.Report;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ReportMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReportService {
    @Resource
    private ReportMapper reportMapper;
    
    @Resource
    private UserService userService;

    @Resource
    private PostService postService;
    
    @Resource
    private CommentService commentService;
    
    @Resource
    private ResourceService resourceService;

    /**
     * 创建举报
     * @param reportDTO 举报信息
     * @param userId 举报用户ID
     */
    public void createReport(ReportDTO reportDTO, Long userId) {
        // 验证用户是否存在
        userService.getUserById(userId);
        
        // 验证举报类型是否合法
        if (reportDTO.getType() < 1 || reportDTO.getType() > 3) {
            throw new ServiceException("举报类型不合法");
        }
        
        // 检查目标是否存在
        checkTargetExists(reportDTO.getType(), reportDTO.getTargetId());
        
        // 创建举报对象
        Report report = new Report();
        report.setUserId(userId);
        report.setType(reportDTO.getType());
        report.setTargetId(reportDTO.getTargetId());
        report.setReason(reportDTO.getReason());
        report.setCreateTime(new Date());
        report.setStatus(0); // 默认状态为未处理
        
        if (reportMapper.insert(report) <= 0) {
            throw new ServiceException("举报创建失败");
        }
    }
    
    /**
     * 处理举报
     * @param id 举报ID
     * @param status 处理状态（1-已处理，2-驳回）
     * @param handlerId 处理人ID
     */
    public void handleReport(Long id, Integer status, Long handlerId) {
        // 验证举报是否存在
        Report report = getReportById(id);
        
        // 验证处理人是否存在
        userService.getUserById(handlerId);
        
        // 验证状态是否合法
        if (status != 1 && status != 2) {
            throw new ServiceException("处理状态不合法");
        }
        
        // 如果是处理（而非驳回），则下架对应的目标
        if (status == 1) {
            takeDownReportTarget(report);
        }
        
        // 更新举报状态
        Report updateReport = new Report();
        updateReport.setId(id);
        updateReport.setStatus(status);
        updateReport.setHandlerId(handlerId);
        updateReport.setHandleTime(new Date());
        
        if (reportMapper.updateById(updateReport) <= 0) {
            throw new ServiceException("举报处理失败");
        }
    }
    
    /**
     * 下架举报目标
     * @param report 举报信息
     */
    private void takeDownReportTarget(Report report) {
        Integer type = report.getType();
        Long targetId = report.getTargetId();
        
        switch (type) {
            case 1: // 帖子
                postService.updatePostStatus(targetId, 0); // 将帖子状态设为删除
                break;
            case 2: // 评论
                commentService.updateCommentStatus(targetId, 0); // 将评论状态设为删除
                break;
            case 3: // 资源
                // 假设资源服务中也有类似的方法
                resourceService.updateResourceStatus(targetId, 0);
                break;
            default:
                throw new ServiceException("举报类型不合法");
        }
    }
    
    /**
     * 分页查询举报
     * @param reason 举报原因（模糊查询）
     * @param type 举报类型
     * @param username 举报用户名
     * @param status 举报状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<Report> getReportsByPage(String reason, Integer type, String username, Integer status, Integer currentPage, Integer size) {
        Page<Report> page = new Page<>(currentPage, size);
        return reportMapper.selectReportPage(page, reason, type, username, status);
    }
    
    /**
     * 根据ID获取举报
     * @param id 举报ID
     * @return 举报信息
     */
    public Report getReportById(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new ServiceException("举报不存在");
        }
        return report;
    }
    
    /**
     * 获取所有举报
     * @return 举报列表
     */
    public List<Report> getReportList() {
        Page<Report> page = new Page<>(1, Integer.MAX_VALUE);
        Page<Report> reportPage = reportMapper.selectReportPage(page, null, null, null, null);
        return reportPage.getRecords();
    }
    
    /**
     * 根据类型和目标ID获取举报
     * @param type 举报类型
     * @param targetId 目标ID
     * @return 举报列表
     */
    public List<Report> getReportsByTypeAndTargetId(Integer type, Long targetId) {
        return reportMapper.selectByTypeAndTargetId(type, targetId);
    }
    
    /**
     * 根据用户ID获取举报
     * @param userId 用户ID
     * @return 举报列表
     */
    public List<Report> getReportsByUserId(Long userId) {
        return reportMapper.selectByUserId(userId);
    }
    
    /**
     * 根据ID删除举报
     * @param id 举报ID
     */
    public void deleteReportById(Long id) {
        if (reportMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 批量删除举报
     * @param ids 举报ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (reportMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
    
    /**
     * 检查目标是否存在
     * @param type 目标类型
     * @param targetId 目标ID
     */
    private void checkTargetExists(Integer type, Long targetId) {
        // 根据类型检查对应的目标是否存在
        switch (type) {
            case 1: // 帖子
                // TODO: 检查帖子是否存在
                break;
            case 2: // 评论
                // TODO: 检查评论是否存在
                break;
            case 3: // 资源
                // TODO: 检查资源是否存在
                break;
            default:
                throw new ServiceException("举报类型不合法");
        }
    }
} 