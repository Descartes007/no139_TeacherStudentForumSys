package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "搜索接口")
@RestController
@RequestMapping("/search")
public class SearchController {
    
    @Resource
    private SearchService searchService;

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public Result<?> getSearchSuggestions(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(null);
        }
        Map<String, Object> suggestions = searchService.getSearchSuggestions(keyword);
        return Result.success(suggestions);
    }

    @Operation(summary = "统一搜索接口")
    @GetMapping
    public Result<?> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String type, // post, resource, section, all
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> results = searchService.search(keyword, type, currentPage, size);
        return Result.success(results);
    }
} 