package org.max.authvs.api.dto.user.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页响应对象
 */
@Schema(description = "分页响应对象")
public record PageVO<T>(
        @Schema(description = "当前页码", example = "1")
        long current,
        @Schema(description = "每页条数", example = "10")
        long size,
        @Schema(description = "总记录数", example = "100")
        long total,
        @Schema(description = "总页数", example = "10")
        long pages,
        @Schema(description = "当前页的数据列表")
        List<T> records
) {
}
