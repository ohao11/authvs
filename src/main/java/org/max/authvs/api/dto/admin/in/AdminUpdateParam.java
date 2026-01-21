package org.max.authvs.api.dto.admin.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新管理员参数")
public class AdminUpdateParam {

    @NotNull(message = "管理员ID不能为空")
    @Schema(description = "管理员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址（可选）", example = "admin@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号码（可选，中国大陆格式）", example = "13800138000")
    private String phone;

    @Schema(description = "是否启用（可选）", example = "true")
    private Boolean enabled;

    @Size(min = 6, max = 255, message = "密码长度必须在6-255字符之间")
    @Schema(description = "新密码（可选，不修改时留空）", example = "newpasswd@123456")
    private String password;

    @Schema(description = "分配的角色ID列表（可选，不修改时留空）", example = "[2, 3]")
    private List<Long> roleIds;
}