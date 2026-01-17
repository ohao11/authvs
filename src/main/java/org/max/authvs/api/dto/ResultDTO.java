package org.max.authvs.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一响应对象，所有接口返回都被该对象包装")
public record ResultDTO<T>(
        @Schema(description = "返回码。200=成功、400=参数错误、401=认证错误、403=权限错误、500=系统错误", example = "200")
        int code,
        @Schema(description = "返回信息说明", example = "success")
        String message,
        @Schema(description = "响应数据，失败时为 null")
        T data
) {

    // 通用返回码
    public static final int SUCCESS = 200;           // 成功
    public static final int BAD_REQUEST = 400;       // 参数错误
    public static final int UNAUTHORIZED = 401;      // 认证错误
    public static final int FORBIDDEN = 403;         // 权限错误
    public static final int SERVER_ERROR = 500;      // 系统错误

    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(SUCCESS, "success", data);
    }

    public static <T> ResultDTO<T> error(int code, String message) {
        return new ResultDTO<>(code, message, null);
    }
}
