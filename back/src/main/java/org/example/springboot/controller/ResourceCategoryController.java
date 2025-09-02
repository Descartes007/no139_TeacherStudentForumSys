package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.ResourceCategoryDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ResourceCategory;
import org.example.springboot.service.ResourceCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "资源分类管理接口")
@RestController
@RequestMapping("/resource/category")
public class ResourceCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCategoryController.class);
    
    @Resource
    private ResourceCategoryService categoryService;

    @Operation(summary = "创建资源分类")
    @PostMapping("/add")
    public Result<?> createCategory(@RequestBody @Valid ResourceCategoryDTO categoryDTO) {
        categoryService.createCategory(categoryDTO);
        return Result.success("分类创建成功");
    }

    @Operation(summary = "更新资源分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ResourceCategoryDTO categoryDTO) {
        categoryService.updateCategory(id, categoryDTO);
        return Result.success("分类更新成功");
    }

    @Operation(summary = "获取所有资源分类")
    @GetMapping("/all")
    public Result<?> getCategoryList() {
        List<ResourceCategory> categories = categoryService.getCategoryList();
        return Result.success(categories);
    }

    @Operation(summary = "获取所有有效资源分类")
    @GetMapping("/valid")
    public Result<?> getAllValidCategories() {
        List<ResourceCategory> categories = categoryService.getAllValidCategories();
        return Result.success(categories);
    }

    @Operation(summary = "分页查询资源分类")
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ResourceCategory> page = categoryService.getCategoriesByPage(categoryName, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取资源分类")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        ResourceCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "更新资源分类状态")
    @PutMapping("/{id}/status")
    public Result<?> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateCategoryStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "根据ID删除资源分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除资源分类")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        categoryService.deleteBatch(ids);
        return Result.success("批量删除成功");
    }
} 