package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PostMapper postMapper;
    
    @Resource
    private CommentMapper commentMapper;
    
    @Resource
    private ResourceMapper resourceMapper;
    
    @Resource
    private SectionMapper sectionMapper;
    
    @Resource
    private ResourceCategoryMapper resourceCategoryMapper;
    
    @Resource
    private ReportMapper reportMapper;
    
    @Resource
    private PostFavoriteMapper postFavoriteMapper;

    /**
     * 获取系统概览数据
     * @return 包含系统各项统计数据的Map
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取各种实体的总数
        long userCount = userMapper.selectCount(null);
        long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1));
        long commentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1));
        long resourceCount = resourceMapper.selectCount(new LambdaQueryWrapper<ForumResource>().eq(ForumResource::getStatus, 1));
        long sectionCount = sectionMapper.selectCount(new LambdaQueryWrapper<Section>().eq(Section::getStatus, 1));
        long reportCount = reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)); // 未处理的举报
        
        // 今日数据统计
        Date todayStart = getTodayStartTime();
        
        long todayUserCount = userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart));
        long todayPostCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .ge(Post::getCreateTime, todayStart));
        long todayCommentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .ge(Comment::getCreateTime, todayStart));
        long todayResourceCount = resourceMapper.selectCount(new LambdaQueryWrapper<ForumResource>()
                .eq(ForumResource::getStatus, 1)
                .ge(ForumResource::getCreateTime, todayStart));
        
        // 添加到结果中
        overview.put("userCount", userCount);
        overview.put("postCount", postCount);
        overview.put("commentCount", commentCount);
        overview.put("resourceCount", resourceCount);
        overview.put("sectionCount", sectionCount);
        overview.put("pendingReportCount", reportCount);
        
        overview.put("todayUserCount", todayUserCount);
        overview.put("todayPostCount", todayPostCount);
        overview.put("todayCommentCount", todayCommentCount);
        overview.put("todayResourceCount", todayResourceCount);
        
        return overview;
    }
    
    /**
     * 获取用户相关统计
     * @return 用户统计数据
     */
    public Map<String, Object> getUsersStatistics() {
        Map<String, Object> userStats = new HashMap<>();
        
        // 用户总数
        long totalUsers = userMapper.selectCount(null);
        
        // 按角色统计用户数量
        LambdaQueryWrapper<User> adminQuery = new LambdaQueryWrapper<User>().eq(User::getRole, 1);
        long adminCount = userMapper.selectCount(adminQuery);
        
        LambdaQueryWrapper<User> teacherQuery = new LambdaQueryWrapper<User>().eq(User::getRole, 2);
        long teacherCount = userMapper.selectCount(teacherQuery);
        
        LambdaQueryWrapper<User> studentQuery = new LambdaQueryWrapper<User>().eq(User::getRole, 3);
        long studentCount = userMapper.selectCount(studentQuery);
        
        // 用户活跃度（基于发帖量）
        List<Map<String, Object>> activeUsers = userMapper.selectUserActivityByPostCount(10);
        
        // 添加到结果中
        userStats.put("totalUsers", totalUsers);
        userStats.put("adminCount", adminCount);
        userStats.put("teacherCount", teacherCount);
        userStats.put("studentCount", studentCount);
        userStats.put("activeUsers", activeUsers);
        
        // 最近注册的用户
        LambdaQueryWrapper<User> recentUserQuery = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime)
                .last("LIMIT 5");
        List<User> recentUsers = userMapper.selectList(recentUserQuery);
        
        // 敏感信息处理
        recentUsers.forEach(user -> {
            user.setPassword(null);
            user.setToken(null);
        });
        
        userStats.put("recentUsers", recentUsers);
        
        return userStats;
    }
    
    /**
     * 获取帖子相关统计
     * @return 帖子统计数据
     */
    public Map<String, Object> getPostsStatistics() {
        Map<String, Object> postStats = new HashMap<>();
        
        // 帖子总数
        long totalPosts = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 1));
        
        // 精华帖数量
        long essencePostCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, 1)
                        .eq(Post::getIsEssence, 1));
        
        // 今日发帖数
        Date todayStart = getTodayStartTime();
        long todayPostCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, 1)
                        .ge(Post::getCreateTime, todayStart));
        
        // 热门帖子（基于浏览量）
        LambdaQueryWrapper<Post> hotPostQuery = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getViewCount)
                .last("LIMIT 5");
        List<Post> hotPosts = postMapper.selectList(hotPostQuery);
        
        // 填充热门帖子信息
        hotPosts.forEach(post -> {
            // 简化帖子信息，只保留需要的字段
            post.setContent(null); // 不需要展示内容
            
            // 获取作者信息
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setAuthorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                post.setAuthorAvatar(user.getAvatar());
            }
            
            // 获取版区信息
            Section section = sectionMapper.selectById(post.getSectionId());
            if (section != null) {
                post.setSectionName(section.getSectionName());
            }
        });
        
        // 统计各版区帖子数量
        List<Map<String, Object>> sectionPostCounts = postMapper.countPostsBySection();
        
        // 添加到结果中
        postStats.put("totalPosts", totalPosts);
        postStats.put("essencePostCount", essencePostCount);
        postStats.put("todayPostCount", todayPostCount);
        postStats.put("hotPosts", hotPosts);
        postStats.put("sectionPostCounts", sectionPostCounts);
        
        return postStats;
    }
    
    /**
     * 获取资源相关统计
     * @return 资源统计数据
     */
    public Map<String, Object> getResourcesStatistics() {
        Map<String, Object> resourceStats = new HashMap<>();
        
        // 资源总数
        long totalResources = resourceMapper.selectCount(new LambdaQueryWrapper<ForumResource>().eq(ForumResource::getStatus, 1));
        
        // 今日上传资源数
        Date todayStart = getTodayStartTime();
        long todayResourceCount = resourceMapper.selectCount(
                new LambdaQueryWrapper<ForumResource>()
                        .eq(ForumResource::getStatus, 1)
                        .ge(ForumResource::getCreateTime, todayStart));
        
        // 资源总大小
        Long totalSize = resourceMapper.sumResourceSize();
        double totalSizeMB = totalSize != null ? totalSize / 1024.0 : 0; // 转换为MB
        
        // 热门资源（基于下载次数）
        LambdaQueryWrapper<ForumResource> hotResourceQuery = new LambdaQueryWrapper<ForumResource>()
                .eq(ForumResource::getStatus, 1)
                .orderByDesc(ForumResource::getDownloadCount)
                .last("LIMIT 5");
        List<ForumResource> hotResources = resourceMapper.selectList(hotResourceQuery);
        
        // 填充热门资源信息
        hotResources.forEach(resource -> {
            // 获取上传者信息
            User user = userMapper.selectById(resource.getUserId());
            if (user != null) {
                resource.setUploadUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
            
            // 获取分类信息
            ResourceCategory category = resourceCategoryMapper.selectById(resource.getCategoryId());
            if (category != null) {
                resource.setCategoryName(category.getCategoryName());
            }
        });
        
        // 统计各分类资源数量
        List<Map<String, Object>> categoryResourceCounts = resourceMapper.countResourcesByCategory();
        
        // 添加到结果中
        resourceStats.put("totalResources", totalResources);
        resourceStats.put("todayResourceCount", todayResourceCount);
        resourceStats.put("totalSizeMB", String.format("%.2f", totalSizeMB));
        resourceStats.put("hotResources", hotResources);
        resourceStats.put("categoryResourceCounts", categoryResourceCounts);
        
        return resourceStats;
    }
    
    /**
     * 获取所有统计数据
     * @return 综合统计数据
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> allStats = new HashMap<>();
        
        allStats.put("overview", getSystemOverview());
        allStats.put("users", getUsersStatistics());
        allStats.put("posts", getPostsStatistics());
        allStats.put("resources", getResourcesStatistics());
        
        return allStats;
    }
    
    /**
     * 获取今天开始的时间
     * @return 今天零点时间
     */
    private Date getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
} 