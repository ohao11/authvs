package org.max.authvs.api.dto.user.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户详细信息（包含RBAC）")
@Data
public class UserDetailVo {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "用户类型：1-门户用户 2-后台管理员")
    private Integer userType;
    
    @Schema(description = "是否启用")
    private Boolean enabled;
    
    @Schema(description = "角色列表")
    private List<RoleVo> roles;
    
    @Schema(description = "权限列表")
    private List<PermissionVo> permissions;
    
    @Data
    @Schema(description = "角色信息")
    public static class RoleVo {
        private Long id;
        private String roleName;
        private String roleCode;
        private Integer roleType;
        private String description;
    }
    
    @Data
    @Schema(description = "权限信息")
    public static class PermissionVo {
        private Long id;
        private String permissionName;
        private String permissionCode;
        private Integer permissionType;
        private String modulePath;
        private String description;
    }
}
