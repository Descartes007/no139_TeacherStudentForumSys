package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ForumResource;
import org.example.springboot.entity.Post;
import org.example.springboot.entity.Section;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    
    @Resource
    private PostService postService;
    
    @Resource
    private ResourceService resourceService;
    
    @Resource
    private SectionService sectionService;

    /**
     * 获取快速搜索建议
     */
    public Map<String, Object> getSearchSuggestions(String keyword) {
        Map<String, Object> suggestions = new HashMap<>();
        
        // 搜索帖子（限制5条）
        Page<Post> posts = postService.getPostsByPage(
            keyword,
            null,
            null,
            null,
            1,
            1,  // currentPage
            5   // size
        );
        suggestions.put("posts", posts.getRecords());
        
        // 搜索版块（限制3条）
        List<Section> sections = sectionService.searchSections(keyword, 3);
        suggestions.put("sections", sections);
        
        // 搜索资源（限制5条）
        Page<ForumResource> resources = resourceService.getResourcesByPage(
            keyword,
            null,
            null,
            null,
            1,
            1,  // currentPage
            5   // size
        );
        suggestions.put("resources", resources.getRecords());
        
        return suggestions;
    }

    /**
     * 完整搜索
     */
    public Map<String, Object> search(String keyword, String type, Integer currentPage, Integer size) {
        Map<String, Object> results = new HashMap<>();
        
        if (type == null || type.equals("all") || type.equals("post")) {
            // 搜索帖子
            Page<Post> posts = postService.getPostsByPage(
                keyword,
                null,
                null,
                null,
                1,
                currentPage,
                size
            );
            results.put("posts", posts);
        }
        
        if (type == null || type.equals("all") || type.equals("section")) {
            // 搜索版块
            List<Section> sections = sectionService.searchSections(keyword, null);
            results.put("sections", sections);
        }
        
        if (type == null || type.equals("all") || type.equals("resource")) {
            // 搜索资源
            Page<ForumResource> resources = resourceService.getResourcesByPage(
                keyword,
                null,
                null,
                null,
                1,
                currentPage,
                size
            );
            results.put("resources", resources);
        }
        
        return results;
    }
} 