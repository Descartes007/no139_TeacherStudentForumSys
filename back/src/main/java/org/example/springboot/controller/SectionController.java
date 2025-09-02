package org.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Section;
import org.example.springboot.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版区控制器
 */
@Tag(name = "版区管理")
@RestController
@RequestMapping("/section")
public class SectionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SectionController.class);

    @Resource
    private SectionService sectionService;

    /**
     * 分页查询版区列表
     */
    @Operation(summary = "分页查询版区列表")
    @GetMapping("/page")
    public Result<IPage<Section>> listSections(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false, defaultValue = "1") Integer status) {
        // 如果status为-1，则查询所有状态
        Integer finalStatus = status == -1 ? null : status;
        IPage<Section> page = sectionService.listSections(currentPage, size,           sectionName, finalStatus);
        return Result.success(page);
    }

    @Operation(summary = "获取所有版区")
    @GetMapping("/all")
    public Result<List<Section>> getall(@RequestParam(required = false, defaultValue = "1") Integer status) {
        // 如果status为-1，则查询所有状态
        Integer finalStatus = status == -1 ? null : status;
        List<Section> sections = sectionService.listSections(1, Integer.MAX_VALUE, null, finalStatus).getRecords();
        return Result.success(sections);
    }

    /**
     * 根据ID获取版区信息
     */
    @Operation(summary = "根据ID获取版区信息")
    @GetMapping("/{id}")
    public Result<Section> getSectionById(
            @PathVariable("id") Long id) {
        Section section = sectionService.getSectionById(id);
        return Result.success(section);
    }

    /**
     * 添加版区
     */
    @Operation(summary = "添加版区")
    @PostMapping("/add")
    public Result<?> addSection(@RequestBody @Valid Section section) {
        if (sectionService.addSection(section)) {
            return Result.success("添加版区成功");
        }
        return Result.error("添加版区失败");
    }

    /**
     * 更新版区
     */
    @Operation(summary = "更新版区")
    @PutMapping("/{id}")
    public Result<?> updateSection(@PathVariable Long id, @RequestBody @Valid Section section) {
        section.setId(id);
        if (sectionService.updateSection(section)) {
            return Result.success("更新版区成功");
        }
        return Result.error("更新版区失败，请检查版区是否存在");
    }

    /**
     * 删除版区
     */
    @Operation(summary = "删除版区")
    @DeleteMapping("/{id}")
    public Result<?> deleteSection(
            @PathVariable("id") Long id) {
        if (sectionService.deleteSection(id)) {
            return Result.success("删除版区成功");
        }
        return Result.error("删除版区失败，请检查版区是否存在或是否有关联数据");
    }
    
    /**
     * 批量删除版区
     */
    @Operation(summary = "批量删除版区")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteSections(@RequestParam List<Long> ids) {
        if (sectionService.batchDeleteSections(ids)) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }
    
    /**
     * 更新版区状态
     */
    @Operation(summary = "更新版区状态")
    @PutMapping("/{id}/status")
    public Result<?> updateSectionStatus(
            @PathVariable("id") Long id,
            @RequestParam Integer status) {
        if (sectionService.updateSectionStatus(id, status)) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
} 