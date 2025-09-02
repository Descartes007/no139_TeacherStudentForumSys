package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户密码更新DTO")
public class UserPasswordUpdateDTO {
    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6到100个字符之间")
    @Schema(description = "新密码")
    private String newPassword;
}
