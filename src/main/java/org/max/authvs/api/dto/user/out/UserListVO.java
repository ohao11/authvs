package org.max.authvs.api.dto.user.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户列表项")
public record UserListVo(
        @Schema(description = "用户ID", example = "1")
        Long id,
        @Schema(description = "用户名", example = "admin")
        String username,
        @Schema(description = "邮箱（已脱敏）", example = "ad***@example.com")
        String email,
        @Schema(description = "手机号（已脱敏）", example = "138****8000")
        String phone,
        @Schema(description = "用户类型：1-门户用户，2-后台用户", example = "2")
        Integer userType,
        @Schema(description = "是否启用", example = "true")
        Boolean enabled,
        @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
        String createdAt
) {
}
