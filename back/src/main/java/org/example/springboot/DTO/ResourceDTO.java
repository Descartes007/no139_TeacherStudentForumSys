package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "资源数据传输对象")
public class ResourceDTO {
    @Schema(description = "资源名称")
    @NotBlank(message = "资源名称不能为空")
    private String resourceName;
    
    @Schema(description = "资源描述")
    private String description;
    
    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    @Schema(description = "文件路径")
    private String filePath;
    
    @Schema(description = "文件大小（KB）")
    private Integer fileSize;
    
    @Schema(description = "文件类型")
    private String fileType;
    
    @Schema(description = "资源状态（0-删除，1-正常）")
    private Integer status;
} 