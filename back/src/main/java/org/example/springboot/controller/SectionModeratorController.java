package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ModeratorDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.SectionModerator;
import org.example.springboot.service.SectionModeratorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "版主管理接口")
@RestController
@RequestMapping("/moderator")
public class SectionModeratorController {
    @Resource
    private SectionModeratorService moderatorService;

    @Operation(summary = "任命版主")
    @PostMapping("/appoint")
    public Result<?> appointModerator(@RequestBody @Valid ModeratorDTO moderatorDTO, @RequestParam Long appointBy) {
        moderatorService.appointModerator(moderatorDTO, appointBy);
        return Result.success("任命成功");
    }

    @Operation(summary = "更新版主信息")
    @PutMapping("/{id}")
    public Result<?> updateModerator(@PathVariable Long id, @RequestBody ModeratorDTO moderatorDTO) {
        moderatorService.updateModerator(id, moderatorDTO);
        return Result.success("更新成功");
    }

    @Operation(summary = "分页查询版主")
    @GetMapping("/page")
    public Result<?> getModeratorsByPage(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SectionModerator> page = moderatorService.getModeratorsByPage(sectionId, username, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取版主")
    @GetMapping("/{id}")
    public Result<?> getModeratorById(@PathVariable Long id) {
        SectionModerator moderator = moderatorService.getModeratorById(id);
        return Result.success(moderator);
    }

    @Operation(summary = "根据版区ID获取版主")
    @GetMapping("/section/{sectionId}")
    public Result<?> getModeratorsBySectionId(@PathVariable Long sectionId) {
        List<SectionModerator> moderators = moderatorService.getModeratorsBySectionId(sectionId);

        return Result.success(moderators);
    }

    @Operation(summary = "根据用户ID获取担任版主的版区")
    @GetMapping("/user/{userId}")
    public Result<?> getModeratorsByUserId(@PathVariable Long userId) {
        List<SectionModerator> moderators = moderatorService.getModeratorsByUserId(userId);
        return Result.success(moderators);
    }

    @Operation(summary = "根据ID删除版主")
    @DeleteMapping("/{id}")
    public Result<?> deleteModerator(@PathVariable Long id) {
        moderatorService.deleteModeratorById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除版主")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        moderatorService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "更新版主状态")
    @PutMapping("/{id}/status")
    public Result<?> updateModeratorStatus(@PathVariable Long id, @RequestParam Integer status) {
        moderatorService.updateModeratorStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "检查用户是否是版主")
    @GetMapping("/check")
    public Result<?> checkModerator(@RequestParam Long sectionId, @RequestParam Long userId) {
        boolean isModerator = moderatorService.isModerator(sectionId, userId);
        return Result.success(isModerator);
    }
} 