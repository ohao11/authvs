package org.max.authvs.service;

import org.max.authvs.api.dto.auth.out.LoginVo;
import org.max.authvs.config.I18nMessageService;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.security.JwtService;
import org.max.authvs.utils.SensitiveDataUtils;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final JwtService jwtService;
    private final I18nMessageService i18nMessageService;

    public AuthService(JwtService jwtService, I18nMessageService i18nMessageService) {
        this.jwtService = jwtService;
        this.i18nMessageService = i18nMessageService;
    }

    /**
     * 处理用户登录逻辑
     */
    public LoginVo handleLogin(CustomUserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        String message = i18nMessageService.getMessage("auth.login.success");

        return new LoginVo(
                userDetails.getId(),
                userDetails.getUsername(),
                SensitiveDataUtils.maskEmail(userDetails.getEmail()),
                SensitiveDataUtils.maskPhone(userDetails.getPhone()),
                userDetails.getUserType(),
                token,
                message
        );
    }

    /**
     * 处理用户登出逻辑
     */
    public void handleLogout(String token) {
        if (token != null && !token.isEmpty()) {
            jwtService.revokeToken(token);
        }
    }
}
