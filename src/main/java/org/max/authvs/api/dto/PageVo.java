package org.max.authvs.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一的分页响应包装类
 * @param <T> 数据项类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "分页数据")
public class PageVo<T> {

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "每页数量")
    private Long pageSize;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "数据列表")
    private List<T> records;
}
