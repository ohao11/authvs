package org.max.authvs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.api.dto.menu.out.MenuVo;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.service.MenuService;
import org.max.authvs.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单控制器
 */
@Tag(name = "菜单管理", description = "菜单相关接口")
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 获取当前用户的菜单
     *
     * @return 菜单树列表
     */
    @Operation(summary = "获取用户菜单", description = "获取当前登录用户有权限访问的菜单")
    @GetMapping("/user-menus")
    public ResultDTO<List<MenuVo>> getUserMenus() {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUser();
        List<MenuVo> menus = menuService.getUserMenus(userDetails.getId());
        return ResultDTO.success(menus);
    }
}
