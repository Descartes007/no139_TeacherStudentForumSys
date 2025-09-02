package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "资源分类数据传输对象")
public class ResourceCategoryDTO {
    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
    
    @Schema(description = "分类描述")
    private String description;
    
    @Schema(description = "分类状态（0-禁用，1-正常）")
    private Integer status;
} 