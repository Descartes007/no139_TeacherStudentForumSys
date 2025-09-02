package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ReportDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Report;
import org.example.springboot.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.util.List;

@RestController
@RequestMapping("/report")
@Tag(name = "举报管理", description = "举报的增删改查")
public class ReportController {
    @Resource
    private ReportService reportService;

    @PostMapping("/add")
    @Operation(summary = "创建举报", description = "创建新的举报")
    public Result createReport(@RequestBody @Valid ReportDTO reportDTO, HttpServletRequest request) {
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
        
        reportService.createReport(reportDTO, userId);
        return Result.success("举报提交成功");
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有举报", description = "获取所有举报列表")
    public Result getReportList() {
        List<Report> reportList = reportService.getReportList();
        return Result.success(reportList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询举报", description = "分页查询举报，支持多条件筛选")
    public Result getReportsByPage(
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Report> page = reportService.getReportsByPage(reason, type, username, status, current, size);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取举报", description = "根据ID获取举报详细信息")
    public Result getReportById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return Result.success(report);
    }

    @GetMapping("/type/{type}/target/{targetId}")
    @Operation(summary = "根据类型和目标ID获取举报", description = "获取指定类型和目标ID的所有举报")
    public Result getReportsByTypeAndTargetId(@PathVariable Integer type, @PathVariable Long targetId) {
        List<Report> reports = reportService.getReportsByTypeAndTargetId(type, targetId);
        return Result.success(reports);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的举报", description = "获取指定用户的所有举报")
    public Result getReportsByUserId(@PathVariable Long userId) {
        List<Report> reports = reportService.getReportsByUserId(userId);
        return Result.success(reports);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除举报", description = "根据ID删除举报")
    public Result deleteReport(@PathVariable Long id) {
        reportService.deleteReportById(id);
        return Result.success("举报删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除举报", description = "批量删除举报")
    public Result deleteBatch(@RequestParam List<Integer> ids) {
        reportService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @PutMapping("/{id}/handle")
    @Operation(summary = "处理举报", description = "处理举报（1-已处理并下架目标，2-驳回）")
    public Result handleReport(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        // 从请求头中获取token并解析用户ID
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            return Result.error("未授权，请先登录");
        }
        
        Long handlerId;
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            handlerId = Long.valueOf(userId);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
        
        reportService.handleReport(id, status, handlerId);
        
        return status == 1 
            ? Result.success("举报处理成功，已下架对应目标") 
            : Result.success("举报已驳回");
    }
} 