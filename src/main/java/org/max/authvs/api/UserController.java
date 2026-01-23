package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.user.in.UserQueryParam;
import org.max.authvs.api.dto.user.out.UserDetailVo;
import org.max.authvs.api.dto.user.out.UserListVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "普通用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "分页查询普通用户列表")
    @Operation(summary = "分页查询普通用户列表", description = "查询门户普通用户列表，支持按用户名、邮箱、手机号等条件查询，需要用户列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_USER_LIST')")
    @PostMapping("/page")
    public ResultDTO<PageVo<UserListVo>> getUsersByPage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户查询参数",
                    required = true)
            @RequestBody UserQueryParam param) {
        PageVo<UserListVo> pageVo = userService.getUsersByPage(param);
        return ResultDTO.success(pageVo);
    }

    @OperationLog(type = OperationType.QUERY, module = "用户管理", description = "查询用户详情")
    @Operation(summary = "获取用户详细信息", description = "根据用户ID查询用户详细信息（包含角色和权限），需要用户列表权限")
    @PreAuthorize("@accessChecker.perm('PERM_USER_LIST')")
    @GetMapping("/{userId}/detail")
    public ResultDTO<UserDetailVo> getUserDetail(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "用户ID无效");
        }
        UserDetailVo userDetail = userService.getUserDetailById(userId);
        if (userDetail == null) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "用户不存在");
        }
        return ResultDTO.success(userDetail);
    }
}