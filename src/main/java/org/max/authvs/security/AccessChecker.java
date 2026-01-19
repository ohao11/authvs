package org.max.authvs.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 * 用于在 @PreAuthorize 中检查权限，自动支持超级管理员
 */
@Component
public class AccessChecker {

    /**
     * 通用权限检查方法
     * 超级管理员或拥有指定权限的用户返回 true
     *
     * @param permission 权限编码
     * @return 是否拥有指定权限
     */
    public boolean perm(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 检查是否为超级管理员
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_SUPER_ADMIN".equals(auth.getAuthority()))) {
            return true;
        }

        // 检查是否拥有指定权限
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> permission.equals(auth.getAuthority()));
    }
}
