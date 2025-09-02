package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Post;
import org.example.springboot.entity.PostFavorite;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PostFavoriteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PostFavoriteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostFavoriteService.class);
    
    @Resource
    private PostFavoriteMapper postFavoriteMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    @Lazy
    private PostService postService;
    
    /**
     * 收藏/取消收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否收藏
     */
    @Transactional
    public boolean toggleFavorite(Long postId, Long userId) {
        // 验证用户是否存在
        User user = userService.getUserById(userId);
        
        // 验证帖子是否存在
        Post post = postService.getPostById(postId);
        
        // 查询收藏记录
        PostFavorite favorite = postFavoriteMapper.selectFavoriteByPostIdAndUserId(postId, userId);
        
        boolean isNowFavorite;
        if (favorite == null) {
            // 不存在收藏记录，创建新记录
            favorite = new PostFavorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            favorite.setCreateTime(new Date());
            favorite.setStatus(1); // 已收藏
            
            if (postFavoriteMapper.insert(favorite) <= 0) {
                throw new ServiceException("收藏操作失败");
            }
            isNowFavorite = true;
        } else {
            // 存在记录，切换状态
            int newStatus = favorite.getStatus() == 1 ? 0 : 1;
            favorite.setStatus(newStatus);
            favorite.setCreateTime(new Date()); // 更新时间
            
            if (postFavoriteMapper.updateById(favorite) <= 0) {
                throw new ServiceException("更新收藏状态失败");
            }
            isNowFavorite = newStatus == 1;
        }
        
        return isNowFavorite;
    }
    
    /**
     * 查询帖子是否被收藏
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否被收藏
     */
    public boolean isFavorited(Long postId, Long userId) {
        PostFavorite favorite = postFavoriteMapper.selectFavoriteByPostIdAndUserId(postId, userId);
        return favorite != null && favorite.getStatus() == 1;
    }
    
    /**
     * 获取帖子的收藏数量
     * @param postId 帖子ID
     * @return 收藏数量
     */
    public int getFavoriteCount(Long postId) {
        Integer count = postFavoriteMapper.countFavoritesByPostId(postId);
        return count != null ? count : 0;
    }
    
    /**
     * 分页查询用户收藏的帖子
     * @param userId 用户ID
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<PostFavorite> getUserFavorites(Long userId, int currentPage, int size) {
        Page<PostFavorite> page = new Page<>(currentPage, size);
        return postFavoriteMapper.selectFavoritesByUserId(page, userId);
    }
    
    /**
     * 取消收藏
     * @param favoriteId 收藏记录ID
     * @param userId 用户ID
     */
    @Transactional
    public void cancelFavorite(Long favoriteId, Long userId) {
        // 查询收藏记录
        PostFavorite favorite = postFavoriteMapper.selectById(favoriteId);
        if (favorite == null) {
            throw new ServiceException("收藏记录不存在");
        }
        
        // 验证是否是用户自己的收藏
        if (!favorite.getUserId().equals(userId)) {
            throw new ServiceException("无权操作他人的收藏");
        }
        
        // 更新收藏状态为取消
        favorite.setStatus(0);
        if (postFavoriteMapper.updateById(favorite) <= 0) {
            throw new ServiceException("取消收藏失败");
        }
    }
} 