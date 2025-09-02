package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户个人资料更新")
public class UserProfileUpdateDTO {
    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "个性签名")
    @Size(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "个人简介")
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String profile;
} 