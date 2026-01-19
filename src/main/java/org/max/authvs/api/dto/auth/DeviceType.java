package org.max.authvs.api.dto.auth;

/**
 * 设备类型枚举
 */
public enum DeviceType {
    WEB("web", "网页端"),
    IOS("ios", "iOS端"),
    ANDROID("android", "Android端"),
    PC("pc", "PC客户端");

    private final String code;
    private final String description;

    DeviceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取设备类型
     */
    public static DeviceType fromCode(String code) {
        if (code == null) {
            return WEB; // 默认为 web
        }
        for (DeviceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return WEB; // 未知类型默认为 web
    }
}
