package org.max.authvs.api.dto.client.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户端查询请求参数
 */
@Data
@Schema(description = "客户端查询参数")
public class ClientQueryParam {

    @Schema(description = "客户端名称（模糊查询）", example = "Web门户")
    private String clientName;

    @Schema(description = "客户端ID（模糊查询）", example = "web")
    private String clientId;

    @Schema(description = "客户端类型：1-Web应用 2-移动应用 3-单页应用 4-服务端应用", example = "1")
    private Integer clientType;

    @Schema(description = "启用状态：0-禁用 1-启用", example = "1")
    private Integer enabled;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
