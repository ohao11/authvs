package org.max.authvs.api.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志响应VO
 */
@Data
@Schema(description = "操作日志响应")
public class OperationLogVo {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作模块")
    private String operationModule;

    @Schema(description = "操作描述")
    private String operationDesc;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "返回结果")
    private String responseResult;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行时长（毫秒）")
    private Long executeTime;

    @Schema(description = "执行状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
