package org.max.authvs.api;

import org.max.authvs.api.dto.user.out.UserDetailVo;
import org.max.authvs.api.dto.user.out.UserVo;
import org.max.authvs.api.dto.user.out.PageVO;
import org.max.authvs.api.dto.user.out.UserListVO;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.PageQuery;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.service.UserService;
import org.max.authvs.utils.SensitiveDataUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResultDTO<UserVo> me(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return ResultDTO.success(new UserVo(
                    customUserDetails.getId(),
                    customUserDetails.getUsername(),
                    SensitiveDataUtils.maskEmail(customUserDetails.getEmail()),
                    SensitiveDataUtils.maskPhone(customUserDetails.getPhone()),
                    customUserDetails.getUserType(),
                    customUserDetails.isEnabled()
            ));
        }
        // 兜底处理
        return ResultDTO.success(new UserVo(null, authentication.getName(), null, null, null, true));
    }

    @Operation(summary = "获取当前用户详细信息", description = "获取当前用户的完整RBAC信息（角色和权限）")
    @GetMapping("/me/detail")
    public ResultDTO<UserDetailVo> meDetail(Authentication authentication) {
        UserDetailVo userDetail = userService.getUserDetailByUsername(authentication.getName());
        if (userDetail == null) {
            return ResultDTO.error(ResultDTO.BAD_REQUEST, "用户不存在");
        }
        return ResultDTO.success(userDetail);
    }

    @Operation(summary = "获取当前用户基本信息", description = "从认证信息直接获取用户基本信息（包含email、phone等）")
    @GetMapping("/me/info")
    public ResultDTO<?> meInfo(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            // 返回包含完整信息的对象（邮箱和手机号脱敏）
            return ResultDTO.success(new UserInfoVo(
                    customUserDetails.getId(),
                    customUserDetails.getUsername(),
                    SensitiveDataUtils.maskEmail(customUserDetails.getEmail()),
                    SensitiveDataUtils.maskPhone(customUserDetails.getPhone()),
                    customUserDetails.getUserType(),
                    customUserDetails.isEnabled(),
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList()
            ));
        }
        return ResultDTO.error(ResultDTO.SERVER_ERROR, "无法获取用户信息");
    }

    /**
     * 用户基本信息VO
     */
    private record UserInfoVo(
            Long id,
            String username,
            String email,
            String phone,
            Integer userType,
            boolean enabled,
            List<String> authorities
    ) {}

    @Operation(summary = "分页查询用户列表", description = "查询后台管理员用户列表，需要用户管理权限或SUPER_ADMIN角色")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USER_MODULE')")
    @PostMapping("/page")
    public ResultDTO<PageVO<UserListVO>> getUsersByPage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "分页查询参数",
                    required = true)
            @RequestBody PageQuery pageQuery) {
        try {
            PageVO<UserListVO> pageVO = userService.getUsersByPage(pageQuery.getPageNum(), pageQuery.getPageSize());
            return ResultDTO.success(pageVO);
        } catch (Exception e) {
            return ResultDTO.error(ResultDTO.SERVER_ERROR, "查询用户列表失败");
        }
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
        try {
            UserDetailVo userDetail = userService.getUserDetailById(userId);
            if (userDetail == null) {
                return ResultDTO.error(ResultDTO.BAD_REQUEST, "用户不存在");
            }
            return ResultDTO.success(userDetail);
        } catch (Exception e) {
            return ResultDTO.error(ResultDTO.SERVER_ERROR, "查询用户详细信息失败");
        }
    }
}