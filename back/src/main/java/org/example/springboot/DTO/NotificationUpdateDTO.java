package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新通知DTO")
public class NotificationUpdateDTO {
    @Schema(description = "通知标题")
    @Size(min = 2, max = 200, message = "通知标题长度必须在2到200个字符之间")
    private String title;
    
    @Schema(description = "通知内容")
    private String content;
    
    @Schema(description = "通知状态（0-下线，1-发布）")
    private Integer status;
} 