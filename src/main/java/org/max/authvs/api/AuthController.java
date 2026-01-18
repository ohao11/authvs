package org.max.authvs.api;

import org.max.authvs.api.dto.auth.in.LoginParam;
import org.max.authvs.api.dto.auth.out.LoginVo;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.config.I18nMessageService;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.security.JwtService;
import org.max.authvs.utils.SensitiveDataUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "身份认证", description = "身份认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final I18nMessageService i18nMessageService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, I18nMessageService i18nMessageService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.i18nMessageService = i18nMessageService;
    }

    @Operation(summary = "用户登录", description = "用户身份认证，返回 JWT token")
    @PostMapping("/login")
    public ResultDTO<LoginVo> login(@Valid @RequestBody LoginParam request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        String message = i18nMessageService.getMessage("auth.login.success");

        return ResultDTO.success(new LoginVo(
                userDetails.getId(),
                userDetails.getUsername(),
                SensitiveDataUtils.maskEmail(userDetails.getEmail()),
                SensitiveDataUtils.maskPhone(userDetails.getPhone()),
                userDetails.getUserType(),
                token,
                message
        ));
    }

    @Operation(summary = "用户登出", description = "登出并使当前令牌失效")
    @PostMapping("/logout")
    public ResultDTO<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
        }
        SecurityContextHolder.clearContext();
        return ResultDTO.success(null);
    }
}