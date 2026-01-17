package org.max.authvs.api.dto.user.out;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "用户个人信息")
public record UserVo(
        @Schema(description = "用户名", example = "admin")
        String username,
        @Schema(description = "用户角色", example = "[\"ADMIN\", \"USER\"]")
        List<String> roles
) {
}
