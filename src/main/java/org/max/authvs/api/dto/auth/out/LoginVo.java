package org.max.authvs.api.dto.auth.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginVo(
        @Schema(description = "JWT 令牌")
        String token,
        @Schema(description = "响应信息")
        String message
) {
}
