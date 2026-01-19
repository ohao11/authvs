package org.max.authvs.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 权限缓存载体，避免重复查询数据库。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCacheVO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private Integer userType;
    private Boolean enabled;
    private List<String> authorities;
    private Long cachedAt;
}
