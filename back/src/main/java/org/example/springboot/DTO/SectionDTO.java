package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "版区DTO")
public class SectionDTO {
    @Schema(description = "版区名称")
    @NotBlank(message = "版区名称不能为空")
    @Size(min = 2, max = 100, message = "版区名称长度必须在2到100个字符之间")
    private String sectionName;
    
    @Schema(description = "版区描述")
    @Size(max = 500, message = "版区描述长度不能超过500个字符")
    private String description;
    
    @Schema(description = "版区状态（0-禁用，1-正常）")
    private Integer status;
} 