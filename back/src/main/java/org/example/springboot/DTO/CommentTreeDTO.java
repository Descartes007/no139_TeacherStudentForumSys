package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.springboot.entity.Comment;
import org.example.springboot.entity.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "评论树DTO")
public class CommentTreeDTO {
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "帖子ID")
    private Long postId;
    
    @Schema(description = "评论用户ID")
    private Long userId;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "评论时间")
    private Date createTime;
    
    @Schema(description = "父评论ID，用于回复评论")
    private Long parentId;
    
    @Schema(description = "评论状态（0-删除，1-正常）")
    private Integer status;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "帖子标题")
    private String postTitle;
    
    @Schema(description = "回复目标用户名")
    private String parentUsername;
    
    @Schema(description = "点赞数量")
    private Integer likeCount;
    
    @Schema(description = "当前用户是否点赞")
    private Boolean isLiked;
    
    @Schema(description = "子评论列表")
    private List<CommentTreeDTO> children = new ArrayList<>();

    @Schema(description = "用户信息")
    private User user;
    
    // 从Comment实体转换为CommentTreeDTO
    public static CommentTreeDTO fromComment(Comment comment) {
        CommentTreeDTO dto = new CommentTreeDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreateTime(comment.getCreateTime());
        dto.setParentId(comment.getParentId());
        dto.setStatus(comment.getStatus());
        dto.setUsername(comment.getUsername());
        dto.setUserAvatar(comment.getUserAvatar());
        dto.setPostTitle(comment.getPostTitle());
        dto.setParentUsername(comment.getParentUsername());
        dto.setLikeCount(comment.getLikeCount());
        dto.setIsLiked(comment.getIsLiked());
        return dto;
    }
} 