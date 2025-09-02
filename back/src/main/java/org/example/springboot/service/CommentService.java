package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.CommentDTO;
import org.example.springboot.DTO.CommentTreeDTO;
import org.example.springboot.entity.Comment;
import org.example.springboot.entity.Post;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CommentMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Resource
    private CommentMapper commentMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private PostService postService;
    
    @Resource
    @Lazy // 添加Lazy注解避免循环依赖
    private CommentLikeService commentLikeService;

    /**
     * 创建新评论
     * @param commentDTO 评论信息
     * @param userId 评论用户ID
     */
    public void createComment(CommentDTO commentDTO, Long userId) {
        // 验证用户是否存在
        User user = userService.getUserById(userId);
        
        // 验证帖子是否存在
        Post post = postService.getPostById(commentDTO.getPostId());
        
        // 如果是回复评论，验证父评论是否存在
        if (commentDTO.getParentId() != null) {
            Comment parentComment = getCommentById(commentDTO.getParentId());
            
            // 验证父评论和子评论是否属于同一帖子
            if (!parentComment.getPostId().equals(commentDTO.getPostId())) {
                throw new ServiceException("回复的评论不属于同一帖子");
            }
            
            // 限制评论最多为2级
            if (parentComment.getParentId() != null) {
                // 如果父评论已经是子评论（有自己的父评论），则将新评论的父ID设置为顶级评论ID
                commentDTO.setParentId(parentComment.getParentId());
            }
        }
        
        // 创建评论对象
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(commentDTO.getPostId());
        comment.setContent(commentDTO.getContent());
        comment.setCreateTime(new Date());
        comment.setParentId(commentDTO.getParentId());
        comment.setStatus(commentDTO.getStatus() != null ? commentDTO.getStatus() : 1);
        
        if (commentMapper.insert(comment) <= 0) {
            throw new ServiceException("评论创建失败");
        }
    }
    
    /**
     * 更新评论信息
     * @param id 评论ID
     * @param commentDTO 更新的评论信息
     */
    public void updateComment(Long id, CommentDTO commentDTO) {
        // 验证评论是否存在
        Comment existingComment = getCommentById(id);
        
        // 更新评论对象
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(commentDTO.getContent());
        comment.setStatus(commentDTO.getStatus());
        
        if (commentMapper.updateById(comment) <= 0) {
            throw new ServiceException("评论更新失败");
        }
    }
    
    /**
     * 获取所有评论列表
     * @return 评论列表
     */
    public List<Comment> getCommentList() {
        Page<Comment> page = new Page<>(1, Integer.MAX_VALUE);
        Page<Comment> commentPage = commentMapper.selectCommentPage(page, null, null,null, null, null);
         commentPage.getRecords().forEach(this::fillUserInfo);
         return commentPage.getRecords();
    }

    /**
     * 填充评论的用户信息
     */
    private void fillUserInfo(Comment comment) {
        if (comment.getUserId() != null) {
            User user = userService.getUserById(comment.getUserId());
            comment.setUser(user);
        }
    }

    /**
     * 分页查询评论
     * @param content 评论内容（模糊查询）
     * @param username 用户名
     * @param postTitle 帖子标题
     * @param status 评论状态
     * @param currentPage 当前页
     * @param size 每页记录数
     * @return 分页结果
     */
    public Page<Comment> getCommentsByPage(String content, String username, String postTitle, Integer postId,Integer status, Integer currentPage, Integer size) {
        Page<Comment> page = new Page<>(currentPage, size);
        Page<Comment> commentPage = commentMapper.selectCommentPage(page, content, username, postTitle, postId, status);
        commentPage.getRecords().forEach(this::fillUserInfo);
        return commentPage;
    }
    
    /**
     * 根据ID获取评论
     * @param id 评论ID
     * @return 评论信息
     */
    public Comment getCommentById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }
        fillUserInfo(comment);
        return comment;
    }
    
    /**
     * 根据帖子ID获取评论列表
     * @param postId 帖子ID
     * @return 评论列表
     */
    public List<Comment> getCommentsByPostId(Long postId) {

        List<Comment> comments = commentMapper.selectByPostId(postId);
        comments.forEach(this::fillUserInfo);
        return comments;
    }
    
    /**
     * 根据用户ID获取评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    public List<Comment> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentMapper.selectByUserId(userId);
        comments.forEach(this::fillUserInfo);
        return comments;
    }
    
    /**
     * 根据父评论ID获取子评论列表
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    public List<Comment> getCommentsByParentId(Long parentId) {
        List<Comment> comments = commentMapper.selectByParentId(parentId);
        comments.forEach(this::fillUserInfo);
        return comments;
    }
    
    /**
     * 根据ID删除评论及其子评论
     * @param id 评论ID
     */
    public void deleteCommentById(Long id) {
        // 先删除子评论
        deleteChildComments(id);
        
        // 再删除评论本身
        if (commentMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
    
    /**
     * 删除指定评论的所有子评论
     * @param parentId 父评论ID
     */
    private void deleteChildComments(Long parentId) {
        // 获取所有子评论
        List<Comment> childComments = getCommentsByParentId(parentId);
        
        // 如果存在子评论，则删除
        if (!childComments.isEmpty()) {
            for (Comment child : childComments) {
                commentMapper.deleteById(child.getId());
            }
        }
    }
    
    /**
     * 用户删除自己的评论
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 是否删除成功
     */
    public boolean deleteUserComment(Long commentId, Long userId) {
        // 验证评论是否存在
        Comment comment = getCommentById(commentId);
        
        // 验证评论是否属于当前用户
        if (!comment.getUserId().equals(userId)) {
            return false;
        }
        
        // 删除评论及其子评论
        deleteCommentById(commentId);
        
        return true;
    }
    
    /**
     * 批量删除评论
     * @param ids 评论ID列表
     */
    public void deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 遍历删除每个评论及其子评论
        for (Integer id : ids) {
            try {
                deleteCommentById(id.longValue());
            } catch (Exception e) {
                // 记录错误但继续删除其他评论
                System.err.println("删除评论ID " + id + " 失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 更新评论状态
     * @param id 评论ID
     * @param status 状态（0-删除，1-正常）
     */
    public void updateCommentStatus(Long id, Integer status) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        
        if (commentMapper.updateById(comment) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }
    
    /**
     * 获取评论信息并包含点赞信息
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 评论信息
     */
    public Comment getCommentWithLikeInfo(Long commentId, Long userId) {
        Comment comment = getCommentById(commentId);
        
        // 获取点赞数量
        int likeCount = commentLikeService.getLikeCount(commentId);
        comment.setLikeCount(likeCount);
        
        // 如果用户已登录，查询点赞状态
        if (userId != null) {
            boolean isLiked = commentLikeService.isLiked(commentId, userId);
            comment.setIsLiked(isLiked);
        }fillUserInfo(comment);
        return comment;
    }
    
    /**
     * 构建评论树结构，并包含点赞信息
     * @param postId 帖子ID
     * @param userId 当前用户ID（可为null）
     * @return 评论树列表
     */
    public List<CommentTreeDTO> getCommentTreeByPostIdWithLikeInfo(Long postId, Long userId) {
        // 获取帖子所有评论
        List<Comment> allComments = getCommentsByPostId(postId);
        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 为每个评论添加点赞信息
        for (Comment comment : allComments) {
            int likeCount = commentLikeService.getLikeCount(comment.getId());
            comment.setLikeCount(likeCount);
            
            if (userId != null) {
                boolean isLiked = commentLikeService.isLiked(comment.getId(), userId);
                comment.setIsLiked(isLiked);
            }
        }
        allComments.forEach(this::fillUserInfo);
        
        // 构建评论树
        Map<Long, List<Comment>> commentsByParentId = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getParentId() == null ? 0L : comment.getParentId()
                ));
        
        // 获取顶级评论
        List<Comment> rootComments = commentsByParentId.getOrDefault(0L, new ArrayList<>());
        rootComments.forEach(this::fillUserInfo);
        // 转换为树形结构
        return rootComments.stream().map(comment -> {
            CommentTreeDTO node = new CommentTreeDTO();
            node.setId(comment.getId());
            node.setPostId(comment.getPostId());
            node.setUserId(comment.getUserId());
            node.setUsername(comment.getUsername());
            node.setUserAvatar(comment.getUserAvatar());
            node.setContent(comment.getContent());
            node.setCreateTime(comment.getCreateTime());
            node.setParentId(comment.getParentId());
            node.setLikeCount(comment.getLikeCount());
            node.setIsLiked(comment.getIsLiked());

            node.setUser(comment.getUser());

            
            // 递归构建子评论
            node.setChildren(buildChildrenTree(comment.getId(), commentsByParentId));
            
            return node;
        }).collect(Collectors.toList());
    }
    
    /**
     * 递归构建子评论树
     * @param parentId 父评论ID
     * @param commentsByParentId 按父ID分组的评论
     * @return 子评论树
     */
    private List<CommentTreeDTO> buildChildrenTree(Long parentId, Map<Long, List<Comment>> commentsByParentId) {
        List<Comment> children = commentsByParentId.getOrDefault(parentId, new ArrayList<>());
        children.forEach(this::fillUserInfo);
        return children.stream().map(comment -> {
            CommentTreeDTO node = new CommentTreeDTO();
            node.setId(comment.getId());
            node.setPostId(comment.getPostId());
            node.setUserId(comment.getUserId());
            node.setUsername(comment.getUsername());
            node.setUserAvatar(comment.getUserAvatar());
            node.setContent(comment.getContent());
            node.setCreateTime(comment.getCreateTime());
            node.setParentId(comment.getParentId());
            node.setParentUsername(comment.getParentUsername());
            node.setLikeCount(comment.getLikeCount());
            node.setIsLiked(comment.getIsLiked());
            node.setUser(comment.getUser());
            // 递归构建更深层次的子评论
            node.setChildren(buildChildrenTree(comment.getId(), commentsByParentId));
            
            return node;
        }).collect(Collectors.toList());
    }
} 