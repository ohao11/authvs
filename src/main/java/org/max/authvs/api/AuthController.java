package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.max.authvs.annotation.OperationLog;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.auth.DeviceType;
import org.max.authvs.api.dto.auth.in.LoginParam;
import org.max.authvs.api.dto.auth.out.LoginVo;
import org.max.authvs.enums.OperationType;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.service.AuthService;
import org.max.authvs.utils.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "身份认证", description = "身份认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @OperationLog(type = OperationType.LOGIN, module = "认证", description = "用户登录")
    @Operation(summary = "用户登录", description = "用户身份认证，返回 JWT token。支持设备类型参数，同一设备类型仅允许一个活跃会话")
    @PostMapping("/login")
    public ResultDTO<LoginVo> login(HttpServletRequest httpRequest,
                                    @Valid @RequestBody LoginParam request) {
        // 将用户信息放入请求属性，便于AOP切面记录登录操作的用户ID和用户名
        httpRequest.setAttribute("OP_USERNAME", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        httpRequest.setAttribute("OP_USER_ID", userDetails.getId());
        DeviceType deviceType = DeviceType.fromCode(request.deviceType());
        LoginVo loginVo = authService.handleLogin(userDetails, deviceType);
        return ResultDTO.success(loginVo);
    }

    @OperationLog(type = OperationType.LOGOUT, module = "认证", description = "用户登出")
    @Operation(summary = "用户登出", description = "登出并使当前令牌失效")
    @PostMapping("/logout")
    public ResultDTO<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        authService.handleLogout(token);
        SecurityUtils.clearContext();
        return ResultDTO.success(null);
    }
}