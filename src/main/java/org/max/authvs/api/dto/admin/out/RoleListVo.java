package org.max.authvs.api.dto.admin.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "角色列表项")
public record RoleListVo(
        @Schema(description = "角色ID")
        Long id,
        @Schema(description = "角色名称")
        String roleName,
        @Schema(description = "角色编码")
        String roleCode,
        @Schema(description = "角色类型")
        Integer roleType,
        @Schema(description = "描述")
        String description,
        @Schema(description = "是否启用")
        Boolean enabled
) {
}
