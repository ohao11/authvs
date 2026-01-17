package org.max.authvs.api.dto.auth.out;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "登录响应")
public record LoginVo(
        @Schema(description = "用户名", example = "admin")
        String username,
        @Schema(description = "用户角色", example = "[\"ADMIN\", \"USER\"]")
        List<String> roles,
        @Schema(description = "JWT 令牌")
        String token,
        @Schema(description = "响应信息")
        String message
) {
}
