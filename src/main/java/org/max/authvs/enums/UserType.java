package org.max.authvs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserType {
    PORTAL(1, "门户用户"),
    ADMIN(2, "后台管理员");

    private final Integer value;
    private final String description;

    /**
     * 根据值获取枚举
     */
    public static UserType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserType userType : UserType.values()) {
            if (userType.value.equals(value)) {
                return userType;
            }
        }
        return null;
    }
}
