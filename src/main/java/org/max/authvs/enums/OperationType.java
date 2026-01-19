package org.max.authvs.enums;

import lombok.Getter;

/**
 * 操作类型枚举
 */
@Getter
public enum OperationType {
    LOGIN("登录"),
    LOGOUT("登出"),
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    QUERY("查询"),
    EXPORT("导出"),
    IMPORT("导入");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

}
