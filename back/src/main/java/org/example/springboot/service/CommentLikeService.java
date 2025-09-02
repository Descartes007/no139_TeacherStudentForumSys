package org.example.springboot.service;

import jakarta.annotation.Resource;
import org.example.springboot.entity.Comment;
import org.example.springboot.entity.CommentLike;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CommentLikeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CommentLikeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentLikeService.class);
    
    @Resource
    private CommentLikeMapper commentLikeMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private CommentService commentService;
    
    /**
     * 点赞/取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否点赞
     */
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        // 验证用户是否存在
        User user = userService.getUserById(userId);
        
        // 验证评论是否存在
        Comment comment = commentService.getCommentById(commentId);
        
        // 查询点赞记录
        CommentLike like = commentLikeMapper.selectLikeByCommentIdAndUserId(commentId, userId);
        
        boolean isNowLiked;
        if (like == null) {
            // 不存在点赞记录，创建新记录
            like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreateTime(new Date());
            like.setStatus(1); // 已点赞
            
            if (commentLikeMapper.insert(like) <= 0) {
                throw new ServiceException("点赞操作失败");
            }
            isNowLiked = true;
        } else {
            // 存在记录，切换状态
            int newStatus = like.getStatus() == 1 ? 0 : 1;
            like.setStatus(newStatus);
            like.setCreateTime(new Date()); // 更新时间
            
            if (commentLikeMapper.updateById(like) <= 0) {
                throw new ServiceException("更新点赞状态失败");
            }
            isNowLiked = newStatus == 1;
        }
        
        return isNowLiked;
    }
    
    /**
     * 查询评论是否被点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否被点赞
     */
    public boolean isLiked(Long commentId, Long userId) {
        CommentLike like = commentLikeMapper.selectLikeByCommentIdAndUserId(commentId, userId);
        return like != null && like.getStatus() == 1;
    }
    
    /**
     * 获取评论的点赞数量
     * @param commentId 评论ID
     * @return 点赞数量
     */
    public int getLikeCount(Long commentId) {
        Integer count = commentLikeMapper.countLikesByCommentId(commentId);
        return count != null ? count : 0;
    }
} 