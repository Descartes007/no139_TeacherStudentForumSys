package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计数据")
public class UserStatsDTO {
    @Schema(description = "帖子数量")
    private Integer postCount;
    
    @Schema(description = "评论数量")
    private Integer commentCount;
    
    @Schema(description = "收藏数量")
    private Integer favoriteCount;
} 