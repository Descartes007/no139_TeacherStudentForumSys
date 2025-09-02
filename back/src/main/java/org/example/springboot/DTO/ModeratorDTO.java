package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "版主DTO")
public class ModeratorDTO {
    @Schema(description = "版区ID")
    @NotNull(message = "版区ID不能为空")
    private Long sectionId;
    
    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @Schema(description = "备注说明")
    @Size(max = 200, message = "备注说明长度不能超过200个字符")
    private String remark;
    
    @Schema(description = "状态（0-停用，1-正常）")
    private Integer status;
} 