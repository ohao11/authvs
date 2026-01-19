package org.max.authvs.utils;

import org.max.authvs.exception.InvalidTokenException;
import org.max.authvs.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 用于获取当前用户信息
 */
public class SecurityUtils {

    private SecurityUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前认证用户
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户详情
     *
     * @throws InvalidTokenException 当用户未认证时抛出
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidTokenException("auth.unauthorized");
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        throw new InvalidTokenException("auth.token.invalid");
    }

    /**
     * 获取当前用户名
     *
     * @throws InvalidTokenException 当用户未认证时抛出
     */
    public static String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidTokenException("auth.unauthorized");
        }
        return authentication.getName();
    }

    /**
     * 判断用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 清除认证信息
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
