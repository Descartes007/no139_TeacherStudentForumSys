package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.PostDTO;
import org.example.springboot.entity.Post;
import org.example.springboot.entity.Section;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PostMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;

@Service
public class PostService {
    @Resource
    private PostMapper postMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private SectionService sectionService;
    
    @Resource
    private PostFavoriteService postFavoriteService;

    /**
     * 创建新帖子
     * @param postDTO 帖子信息
     * @param userId 发帖用户ID
     */
    public void createPost(PostDTO postDTO, Long userId) {
        // 验证用户是否存在
        User user = userService.getUserById(userId);
        
        // 验证版区是否存在
        Section section = sectionService.getSectionById(postDTO.getSectionId());
        
        // 创建帖子对象
        Post post = new Post();
        post.setUserId(userId);
        post.setSectionId(postDTO.getSectionId());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCreateTime(new Date());
        post.setViewCount(0);
        post.setIsEssence(postDTO.getIsEssence() != null ? postDTO.getIsEssence() : 0);
        post.setStatus(postDTO.getStatus() != null ? postDTO.getStatus() : 1);
        
        if (postMapper.insert(post) <= 0) {
            throw new ServiceException("帖子创建失败");
        }
    }
    
    /**
     * 更新帖子信息
     * @param id 帖子ID
     * @param postDTO 更新的帖子信息
     */
    public void updatePost(Long id, PostDTO postDTO) {
        // 验证帖子是否存在
        Post existingPost = getPostById(id);
        if (existingPost == null) {
            throw new ServiceException("帖子不存在");
        }

        // 验证版区是否存在
        if (postDTO.getSectionId() != null) {
            sectionService.getSectionById(postDTO.getSectionId());
        }
        
        // 更新帖子对象
        Post post = new Post();
        post.setId(id);
        post.setSectionId(postDTO.getSectionId());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setUpdateTime(new Date());
        post.setIsEssence(postDTO.getIsEssence());
        post.setStatus(postDTO.getStatus());
        
        if (postMapper.updateById(post) <= 0) {
            throw new ServiceException("帖子更新失败");
        }
    }
    
    /**
     * 获取所有帖子列表
     * @return 帖子列表
     */
    public List<Post> getPostList() {
        Page<Post> page = new Page<>(1, Integer.MAX_VALUE);
        Page<Post> postPage = postMapper.selectPostPage(page, null, null, null, null, null);
        List<Post> records = postPage.getRecords();
        records.forEach(this::fillUserAndSectionInfo);
        return records;
    }

    public Page<Post> getPostByViewCount() {
        Page<Post> page = new Page<>(1, Integer.MAX_VALUE);
        Page<Post> postPage = postMapper.selectPostPage(page, null, null, null, null, null);
        List<Post> records = postPage.getRecords();
        records.sort((o1, o2) -> o2.getViewCount().compareTo(o1.getViewCount()));
        records.forEach(this::fillUserAndSectionInfo);
        postPage.setRecords(records);
        return postPage;

    }
    
    /**
     * 分页查询帖子
     * @param title 帖子标题（模糊查询）
     * @param userId 用户ID
     * @param sectionId 版区ID
     * @param isEssence 是否精华
     * @param status 帖子状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<Post> getPostsByPage(String title, Long userId, Long sectionId, Integer isEssence, Integer status, Integer currentPage, Integer size) {
        Page<Post> page = new Page<>(currentPage, size);
        Page<Post> postPage = postMapper.selectPostPage(page, title, userId, sectionId, isEssence, status);
        postPage.getRecords().forEach(this::fillUserAndSectionInfo);

        return postPage;
    }
    
    /**
     * 根据ID获取帖子
     * @param id 帖子ID
     * @return 帖子信息
     */
    public Post getPostById(Long id) {
        // 使用增强查询获取帖子及关联信息
//        Page<Post> page = new Page<>(1, 1);
//        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Post::getId, id);
//
//        Page<Post> postPage = postMapper.selectPostPage(page, null, null, null, null, null);
//
//        if (postPage.getRecords().isEmpty()) {
//            throw new ServiceException("帖子不存在");
//        }
//
//        Post post = postPage.getRecords().get(0);
        Post post = postMapper.selectById(id);
        fillUserAndSectionInfo(post);
        return post;
    }
    
    /**
     * 根据用户ID获取帖子列表
     * @param userId 用户ID
     * @return 帖子列表
     */
    public List<Post> getPostsByUserId(Long userId) {
        List<Post> posts = postMapper.selectByUserId(userId);
        posts.forEach(this::fillUserAndSectionInfo);
        return posts;
    }
    
    /**
     * 根据版区ID获取帖子列表
     * @param sectionId 版区ID
     * @return 帖子列表
     */
    public List<Post> getPostsBySectionId(Long sectionId) {

        if (sectionId == null) {
            throw new ServiceException("版区ID不能为空");
        }

        List<Post> posts = postMapper.selectBySectionId(sectionId);
        posts.forEach(this::fillUserAndSectionInfo);
        return posts;
    }
    
    /**
     * 根据ID删除帖子
     * @param id 帖子ID
     */
    public void deletePostById(Long id) {
        if (postMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 批量删除帖子
     * @param ids 帖子ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (postMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }
    
    /**
     * 更新帖子状态
     * @param id 帖子ID
     * @param status 状态（0-删除，1-正常）
     */
    public void updatePostStatus(Long id, Integer status) {
        Post post = new Post();
        post.setId(id);
        post.setStatus(status);
        post.setUpdateTime(new Date());
        
        if (postMapper.updateById(post) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }
    
    /**
     * 设置/取消精华帖
     * @param id 帖子ID
     * @param isEssence 是否精华（0-否，1-是）
     */
    public void updatePostEssence(Long id, Integer isEssence) {
        Post post = new Post();
        post.setId(id);
        post.setIsEssence(isEssence);
        post.setUpdateTime(new Date());
        
        if (postMapper.updateById(post) <= 0) {
            throw new ServiceException("设置精华状态失败");
        }
    }
    
    /**
     * 增加帖子浏览次数
     * @param id 帖子ID
     */
    public void incrementViewCount(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new ServiceException("帖子不存在");
        }
        
        post.setViewCount(post.getViewCount() + 1);
        
        if (postMapper.updateById(post) <= 0) {
            throw new ServiceException("更新浏览次数失败");
        }
    }

    /**
     * 根据ID获取帖子（包含收藏信息）
     * @param id 帖子ID
     * @param userId 当前用户ID（可为null）
     * @return 帖子信息
     */
    public Post getPostDetailById(Long id, Long userId) {
        // 使用增强查询获取帖子及关联信息
        Post post = getPostById(id);
        
        // 如果用户已登录，查询收藏状态
        if (userId != null) {
            boolean isFavorited = postFavoriteService.isFavorited(id, userId);
            post.setIsFavorited(isFavorited);
        }
        
        // 获取收藏数量
        int favoriteCount = postFavoriteService.getFavoriteCount(id);
        post.setFavoriteCount(favoriteCount);
        fillUserAndSectionInfo(post);
        return post;
    }

    /**
     * 填充用户信息和版区信息
     * @param post 帖子
     */

    private void fillUserAndSectionInfo(Post post) {
        if (post.getUserId()!= null) {
            User user = userService.getUserById(post.getUserId());
            post.setUser(user);
        }
        if (post.getSectionId() != null) {
            Section section = sectionService.getSectionById(post.getSectionId());
            post.setSection(section);
        }
    }

    /**
     * 获取板块帖子统计信息
     * @param sectionId 板块ID
     * @return 统计信息
     */
    public Map<String, Object> getSectionPostStats(Long sectionId) {
        // 构建查询条件
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getSectionId, sectionId)
                    .eq(Post::getStatus, 1); // 只统计正常状态的帖子

        // 获取总帖子数
        long totalPosts = postMapper.selectCount(queryWrapper);

        // 获取今日新增帖子数
        // 获取今天的开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        queryWrapper.ge(Post::getCreateTime, todayStart);
        long todayPosts = postMapper.selectCount(queryWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", totalPosts);
        stats.put("todayPosts", todayPosts);

        return stats;
    }
} 