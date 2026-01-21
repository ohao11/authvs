package org.max.authvs.api.dto.admin.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建管理员参数")
public class AdminCreateParam {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin_user")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 255, message = "密码长度必须在6-255字符之间")
    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin@123456")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址（可选）", example = "admin@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号码（可选，中国大陆格式）", example = "13800138000")
    private String phone;

    @Schema(description = "是否启用（默认true）", defaultValue = "true", example = "true")
    private Boolean enabled = Boolean.TRUE;

    @NotEmpty(message = "角色ID列表不能为空")
    @Schema(description = "分配的角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2, 3]")
    private List<Long> roleIds;
}