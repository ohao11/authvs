package org.max.authvs.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.menu.out.MenuVo;
import org.max.authvs.entity.Permission;
import org.max.authvs.entity.Role;
import org.max.authvs.entity.RolePermission;
import org.max.authvs.entity.UserRole;
import org.max.authvs.mapper.PermissionMapper;
import org.max.authvs.mapper.RoleMapper;
import org.max.authvs.mapper.RolePermissionMapper;
import org.max.authvs.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务
 */
@Service
@Slf4j
public class MenuService {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;

    public MenuService(UserRoleMapper userRoleMapper,
                       RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper,
                       RoleMapper roleMapper) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 获取用户的菜单树
     *
     * @param userId 用户ID
     * @return 菜单树列表
     */
    public List<MenuVo> getUserMenus(Long userId) {
        // 1. 获取用户的所有角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取角色信息，判断是否是超级管理员
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getId, roleIds)
        );

        // 如果是超级管理员，返回所有菜单
        boolean isSuperAdmin = roles.stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getRoleCode()));

        List<Permission> permissions;
        if (isSuperAdmin) {
            // 超级管理员：获取所有启用的后台权限
            permissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<Permission>()
                            .eq(Permission::getPermissionType, 2)  // 后台权限
                            .eq(Permission::getEnabled, true)
                            .orderByAsc(Permission::getSortOrder)
            );
        } else {
            // 普通用户：根据角色获取权限
            List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds)
            );

            List<Long> permissionIds = rolePermissions.stream()
                    .map(RolePermission::getPermissionId)
                    .collect(Collectors.toList());

            if (permissionIds.isEmpty()) {
                return new ArrayList<>();
            }

            permissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
                            .eq(Permission::getEnabled, true)
                            .orderByAsc(Permission::getSortOrder)
            );
        }

        if (permissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 构建菜单树（使用hutool TreeUtil）
        return buildMenuTree(permissions);
    }

    /**
     * 构建菜单树
     *
     * @param permissions 权限列表
     * @return 菜单树
     */
    private List<MenuVo> buildMenuTree(List<Permission> permissions) {
        TreeNodeConfig config = new TreeNodeConfig();
        List<Tree<Integer>> result = TreeUtil.build(permissions, 0, config, (obj, tree) -> {
            tree.setId(Math.toIntExact(obj.getId()));
            tree.setParentId(Math.toIntExact(obj.getParentId()));
            tree.setWeight(obj.getSortOrder());
            tree.setName(obj.getPermissionName());
            tree.putExtra("code", obj.getPermissionCode());
            tree.putExtra("path", obj.getModulePath());
            tree.putExtra("sort", obj.getSortOrder());
        });

        return BeanUtil.copyToList(result, MenuVo.class, CopyOptions.create());
    }

}
