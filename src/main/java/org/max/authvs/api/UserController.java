package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.max.authvs.api.dto.PageQuery;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.user.out.PageVO;
import org.max.authvs.api.dto.user.out.UserDetailVo;
import org.max.authvs.api.dto.user.out.UserListVO;
import org.max.authvs.api.dto.user.out.UserVo;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.service.UserService;
import org.max.authvs.utils.SecurityUtils;
import org.max.authvs.utils.SensitiveDataUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取当前用户", description = "获取当前认证用户的个人信息")
    @GetMapping("/me")
    public ResultDTO<UserVo> me() {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUser();
        UserVo userVo = new UserVo(
                userDetails.getId(),
                userDetails.getUsername(),
                SensitiveDataUtils.maskEmail(userDetails.getEmail()),
                SensitiveDataUtils.maskPhone(userDetails.getPhone()),
                userDetails.getUserType(),
                userDetails.isEnabled()
        );
        return ResultDTO.success(userVo);
    }

    @Operation(summary = "分页查询用户列表", description = "查询后台管理员用户列表，需要用户管理权限或SUPER_ADMIN角色")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USER_MODULE')")
    @PostMapping("/page")
    public ResultDTO<PageVO<UserListVO>> getUsersByPage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "分页查询参数",
                    required = true)
            @RequestBody PageQuery pageQuery) {
        PageVO<UserListVO> pageVO = userService.getUsersByPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        return ResultDTO.success(pageVO);
    }

    @Operation(summary = "获取用户详细信息", description = "根据用户ID查询用户详细信息（包含角色和权限），需要用户管理权限或SUPER_ADMIN角色")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USER_MODULE')")
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