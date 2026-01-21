package org.max.authvs.api.dto.admin.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "管理员列表信息")
public record AdminListVo(
        @Schema(description = "管理员ID")
        Long id,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "邮箱（脱敏）")
        String email,
        @Schema(description = "手机号（脱敏）")
        String phone,
        @Schema(description = "是否启用：0-禁用 1-启用")
        Integer enabled,
        @Schema(description = "创建时间")
        String createdAt,
        @Schema(description = "角色列表")
        List<RoleVo> roles
) {

    @Schema(description = "角色信息")
    public record RoleVo(
            @Schema(description = "角色ID")
            Long id,
            @Schema(description = "角色名称")
            String roleName,
            @Schema(description = "角色编码")
            String roleCode
    ) {
    }
}
