package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "评论DTO")
public class CommentDTO {
    @Schema(description = "帖子ID")
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    @Schema(description = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    @Schema(description = "父评论ID，用于回复评论")
    private Long parentId;
    
    @Schema(description = "评论状态（0-删除，1-正常）")
    private Integer status;
} 