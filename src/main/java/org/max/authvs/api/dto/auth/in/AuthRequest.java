package org.max.authvs.api.dto.auth.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "登录请求")
public record AuthRequest(
        @Schema(description = "用户名", example = "admin")
        @NotBlank(message = "username is required") String username,
        @Schema(description = "密码", example = "admin123")
        @NotBlank(message = "password is required") String password
) {
}
