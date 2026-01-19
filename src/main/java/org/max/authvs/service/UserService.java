package org.max.authvs.service;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.user.in.UserQueryParam;
import org.max.authvs.api.dto.user.out.PageVO;
import org.max.authvs.api.dto.user.out.UserDetailVo;
import org.max.authvs.api.dto.user.out.UserListVO;
import org.max.authvs.entity.*;
import org.max.authvs.mapper.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public UserService(UserMapper userMapper,
                       UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper,
                       RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 分页查询用户列表
     */
    /**
     * 分页查询用户列表
     */
    public PageVO<UserListVO> getUsersByPage(UserQueryParam param) {
        Page<User> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUserType, 2); // 只查询后台管理员用户

        // 添加动态查询条件
        if (StrUtil.isNotBlank(param.getUsername())) {
            queryWrapper.like(User::getUsername, param.getUsername());
        }
        if (StrUtil.isNotBlank(param.getEmail())) {
            queryWrapper.like(User::getEmail, param.getEmail());
        }
        if (StrUtil.isNotBlank(param.getPhone())) {
            queryWrapper.like(User::getPhone, param.getPhone());
        }
        if (param.getEnabled() != null) {
            queryWrapper.eq(User::getEnabled, param.getEnabled());
        }

        queryWrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(page, queryWrapper);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<UserListVO> userListVOs = userPage.getRecords().stream()
                .map(user -> new UserListVO(
                        user.getId(),
                        user.getUsername(),
                        DesensitizedUtil.email(user.getEmail()),
                        DesensitizedUtil.mobilePhone(user.getPhone()),
                        user.getUserType(),
                        user.getEnabled(),
                        user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null
                ))
                .collect(Collectors.toList());

        return new PageVO<>(
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal(),
                userPage.getPages(),
                userListVOs
        );
    }

    /**
     * 根据用户ID获取用户详细信息
     */
    public UserDetailVo getUserDetailById(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return null;
        }

        UserDetailVo userDetailVo = new UserDetailVo();
        userDetailVo.setId(user.getId());
        userDetailVo.setUsername(user.getUsername());
        userDetailVo.setEmail(user.getEmail());
        userDetailVo.setPhone(user.getPhone());
        userDetailVo.setUserType(user.getUserType());
        userDetailVo.setEnabled(user.getEnabled());

        // 2. 查询用户的角色ID列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId())
        );

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            userDetailVo.setRoles(new ArrayList<>());
            userDetailVo.setPermissions(new ArrayList<>());
            return userDetailVo;
        }

        // 3. 查询角色详情
        List<Role> roles = roleMapper.selectByIds(roleIds);
        List<UserDetailVo.RoleVo> roleVos = roles.stream()
                .filter(role -> Boolean.TRUE.equals(role.getEnabled()))
                .map(role -> {
                    UserDetailVo.RoleVo roleVo = new UserDetailVo.RoleVo();
                    roleVo.setId(role.getId());
                    roleVo.setRoleName(role.getRoleName());
                    roleVo.setRoleCode(role.getRoleCode());
                    roleVo.setRoleType(role.getRoleType());
                    roleVo.setDescription(role.getDescription());
                    return roleVo;
                })
                .collect(Collectors.toList());
        userDetailVo.setRoles(roleVos);

        // 4. 查询角色对应的权限ID列表
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleId, roleIds)
        );

        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询权限详情
        if (permissionIds.isEmpty()) {
            userDetailVo.setPermissions(new ArrayList<>());
            return userDetailVo;
        }

        List<Permission> permissions = permissionMapper.selectByIds(permissionIds);
        List<UserDetailVo.PermissionVo> permissionVos = permissions.stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getEnabled()))
                .map(permission -> {
                    UserDetailVo.PermissionVo permissionVo = new UserDetailVo.PermissionVo();
                    permissionVo.setId(permission.getId());
                    permissionVo.setPermissionName(permission.getPermissionName());
                    permissionVo.setPermissionCode(permission.getPermissionCode());
                    permissionVo.setPermissionType(permission.getPermissionType());
                    permissionVo.setModulePath(permission.getModulePath());
                    permissionVo.setDescription(permission.getDescription());
                    return permissionVo;
                })
                .collect(Collectors.toList());
        userDetailVo.setPermissions(permissionVos);

        return userDetailVo;
    }
}

