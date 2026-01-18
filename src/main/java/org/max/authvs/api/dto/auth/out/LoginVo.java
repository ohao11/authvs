package org.max.authvs.api.dto.auth.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginVo(
        @Schema(description = "用户ID")
        Long id,
        @Schema(description = "用户名", example = "admin")
        String username,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机号")
        String phone,
        @Schema(description = "用户类型：1-门户用户 2-后台管理员")
        Integer userType,
        @Schema(description = "JWT 令牌")
        String token,
        @Schema(description = "响应信息")
        String message
) {
}
