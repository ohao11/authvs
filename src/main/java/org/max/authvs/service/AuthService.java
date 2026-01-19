package org.max.authvs.service;

import org.max.authvs.api.dto.auth.DeviceType;
import org.max.authvs.api.dto.auth.out.LoginVo;
import org.max.authvs.config.I18nMessageService;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.security.JwtService;
import org.max.authvs.security.PermissionCacheService;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final JwtService jwtService;
    private final I18nMessageService i18nMessageService;
    private final PermissionCacheService permissionCacheService;

    public AuthService(JwtService jwtService,
                       I18nMessageService i18nMessageService,
                       PermissionCacheService permissionCacheService) {
        this.jwtService = jwtService;
        this.i18nMessageService = i18nMessageService;
        this.permissionCacheService = permissionCacheService;
    }

    /**
     * 处理用户登录逻辑
     *
     * @param userDetails 用户详情
     * @param deviceType  设备类型
     * @return 登录响应
     */
    public LoginVo handleLogin(CustomUserDetails userDetails, DeviceType deviceType) {
        String token = jwtService.generateToken(userDetails, deviceType, userDetails.getId());
        String message = i18nMessageService.getMessage("auth.login.success");
        return new LoginVo(token, message);
    }

    /**
     * 处理用户登出逻辑
     * 1. 撤销 token
     * 2. 清除权限缓存
     */
    public void handleLogout(String token) {
        if (token != null && !token.isEmpty()) {
            // 撤销 token
            jwtService.revokeToken(token);

            // 清除权限缓存（从 token 中提取用户名）
            try {
                String username = jwtService.extractUsername(token);
                if (username != null && !username.isEmpty()) {
                    permissionCacheService.clearCache(username);
                }
            } catch (Exception e) {
                // 忽略 token 解析错误，不影响登出流程
            }
        }
    }
}
