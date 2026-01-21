package org.max.authvs.api.dto.admin.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "管理员详细信息")
public record AdminDetailVo(
        @Schema(description = "管理员ID")
        Long id,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "邮箱（脱敏）")
        String email,
        @Schema(description = "手机号（脱敏）")
        String phone,
        @Schema(description = "是否启用")
        Boolean enabled,
        @Schema(description = "管理员角色列表")
        List<RoleVo> roles,
        @Schema(description = "权限列表")
        List<PermissionVo> permissions
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

    @Schema(description = "权限信息")
    public record PermissionVo(
            @Schema(description = "权限ID")
            Long id,
            @Schema(description = "权限名称")
            String permissionName,
            @Schema(description = "权限编码")
            String permissionCode
    ) {
    }
}
