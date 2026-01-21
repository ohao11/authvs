package org.max.authvs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型枚举
 */
@Getter
@AllArgsConstructor
public enum RoleType {
    PORTAL(1, "门户角色"),
    ADMIN(2, "后台管理员角色");

    private final Integer value;
    private final String description;

    /**
     * 根据值获取枚举
     */
    public static RoleType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (RoleType roleType : RoleType.values()) {
            if (roleType.value.equals(value)) {
                return roleType;
            }
        }
        return null;
    }
}
