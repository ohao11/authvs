package org.max.authvs.api.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.max.authvs.api.dto.PageQuery;

import java.time.LocalDateTime;

/**
 * 操作日志查询参数
 */
@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Schema(description = "操作日志查询参数")
public class OperationLogQueryParam extends PageQuery {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作模块")
    private String operationModule;

    @Schema(description = "执行状态：SUCCESS FAIL")
    private String status;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "平台类型：1-门户 2-后台管理", example = "2")
    private Integer platformType;
}
