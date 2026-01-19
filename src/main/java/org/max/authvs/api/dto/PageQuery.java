package org.max.authvs.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询通用入参对象
 */
@Data
@NoArgsConstructor
@Schema(description = "分页查询入参")
public class PageQuery {

    @Schema(description = "页码，从1开始", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数，默认10，最大1000", example = "10")
    private Long pageSize = 10L;


    public Long getPageSize() {
        return pageSize > 1000 ? 1000 : pageSize;
    }

}
