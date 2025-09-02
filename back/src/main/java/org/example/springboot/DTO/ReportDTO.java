package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "举报信息DTO")
public class ReportDTO {
    @Schema(description = "举报类型（1-帖子，2-评论，3-资源）")
    @NotNull(message = "举报类型不能为空")
    private Integer type;
    
    @Schema(description = "举报目标ID")
    @NotNull(message = "举报目标ID不能为空")
    private Long targetId;
    
    @Schema(description = "举报原因")
    @NotBlank(message = "举报原因不能为空")
    private String reason;
} 