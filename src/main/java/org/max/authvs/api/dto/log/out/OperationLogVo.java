package org.max.authvs.api.dto.log.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "操作日志响应")
public record OperationLogVo(
        @Schema(description = "日志ID")
        Long id,
        @Schema(description = "操作用户ID")
        Long userId,
        @Schema(description = "操作用户名")
        String username,
        @Schema(description = "操作类型")
        String operationType,
        @Schema(description = "操作模块")
        String operationModule,
        @Schema(description = "操作描述")
        String operationDesc,
        @Schema(description = "请求方法")
        String requestMethod,
        @Schema(description = "请求URL")
        String requestUrl,
        @Schema(description = "请求参数")
        String requestParams,
        @Schema(description = "返回结果")
        String responseResult,
        @Schema(description = "IP地址")
        String ipAddress,
        @Schema(description = "设备类型")
        String deviceType,
        @Schema(description = "用户代理")
        String userAgent,
        @Schema(description = "平台类型：1-门户 2-后台管理")
        Integer platformType,
        @Schema(description = "执行时长（毫秒）")
        Long executeTime,
        @Schema(description = "执行状态")
        String status,
        @Schema(description = "错误信息")
        String errorMessage,
        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
}
