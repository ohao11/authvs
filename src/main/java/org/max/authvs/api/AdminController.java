package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.admin.in.AdminCreateParam;
import org.max.authvs.api.dto.admin.in.AdminQueryParam;
import org.max.authvs.api.dto.admin.in.AdminUpdateParam;
import org.max.authvs.api.dto.admin.out.AdminDetailVo;
import org.max.authvs.api.dto.admin.out.AdminListVo;
import org.max.authvs.api.dto.admin.out.RoleListVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员管理", description = "管理员相关接口")
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "分页查询管理员列表")
    @Operation(summary = "分页查询管理员列表", description = "查询后台管理员列表，支持按用户名、邮箱、手机号等条件查询，需要管理员列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_ADMIN_LIST')")
    @PostMapping("/page")
    public ResultDTO<PageVo<AdminListVo>> getAdminsByPage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "管理员查询参数",
                    required = true)
            @Valid @RequestBody AdminQueryParam param) {
        PageVo<AdminListVo> pageVo = adminService.getAdminsByPage(param);
        return ResultDTO.success(pageVo);
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "查询管理员详情")
    @Operation(summary = "获取管理员详细信息", description = "根据管理员ID查询管理员详细信息（包含角色和权限），需要管理员列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_ADMIN_LIST')")
    @GetMapping("/{adminId}/detail")
    public ResultDTO<AdminDetailVo> getAdminDetail(
            @Parameter(description = "管理员ID", example = "1")
            @PathVariable Long adminId) {
        if (adminId == null || adminId <= 0) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "管理员ID无效");
        }
        AdminDetailVo detail = adminService.getAdminDetail(adminId);
        if (detail == null) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "管理员不存在");
        }
        return ResultDTO.success(detail);
    }

    @OperationLog(type = OperationType.CREATE, module = "用户管理", description = "创建管理员")
    @Operation(summary = "创建管理员", description = "创建后台管理员账号并分配角色，需要管理员列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_ADMIN_LIST')")
    @PostMapping
    public ResultDTO<Long> createAdmin(@Valid @RequestBody AdminCreateParam param) {
        Long adminId = adminService.createAdmin(param);
        return ResultDTO.success(adminId);
    }

    @OperationLog(type = OperationType.UPDATE, module = "用户管理", description = "更新管理员信息")
    @Operation(summary = "更新管理员信息", description = "更新后台管理员账号信息和角色，需要管理员列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_ADMIN_LIST')")
    @PutMapping("/{adminId}")
    public ResultDTO<Boolean> updateAdmin(
            @Parameter(description = "管理员ID", example = "1")
            @PathVariable Long adminId,
            @Valid @RequestBody AdminUpdateParam param) {
        if (adminId == null || adminId <= 0) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "管理员ID无效");
        }
        param.setId(adminId);
        boolean updated = adminService.updateAdmin(param);
        return ResultDTO.success(updated);
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "获取管理员角色列表")
    @Operation(summary = "获取管理员角色列表", description = "获取所有启用的角色列表，用于创建或编辑管理员时选择角色，需要管理员列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_ADMIN_LIST')")
    @GetMapping("/roles")
    public ResultDTO<List<RoleListVo>> getRolesList() {
        List<RoleListVo> roles = adminService.getRolesList();
        return ResultDTO.success(roles);
    }
}
