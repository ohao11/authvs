package org.max.authvs.api.dto.user.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户详细信息（包含RBAC）")
public record UserDetailVo(
        @Schema(description = "用户ID")
        Long id,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机号")
        String phone,
        @Schema(description = "用户类型：1-门户用户 2-后台管理员")
        Integer userType,
        @Schema(description = "是否启用")
        Boolean enabled,
        @Schema(description = "角色列表")
        List<RoleVo> roles,
        @Schema(description = "权限列表")
        List<PermissionVo> permissions
) {
    @Schema(description = "角色信息")
    public record RoleVo(
            Long id,
            String roleName,
            String roleCode,
            Integer roleType,
            String description
    ) {
    }

    @Schema(description = "权限信息")
    public record PermissionVo(
            Long id,
            String permissionName,
            String permissionCode,
            Integer permissionType,
            String modulePath,
            String description
    ) {
    }
}
