package org.max.authvs.api.dto.menu.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "菜单信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuVo {
    @Schema(description = "菜单ID")
    Long id;
    @Schema(description = "菜单名称")
    String name;
    @Schema(description = "菜单编码")
    String code;
    @Schema(description = "菜单路径")
    String path;
    @Schema(description = "排序")
    Integer sort;
    @Schema(description = "父菜单ID")
    Long parentId;
    @Schema(description = "子菜单列表")
    List<MenuVo> children;

}
