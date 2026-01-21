package org.max.authvs.service;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.user.in.UserQueryParam;
import org.max.authvs.api.dto.user.out.UserDetailVo;
import org.max.authvs.api.dto.user.out.UserListVo;
import org.max.authvs.entity.*;
import org.max.authvs.enums.UserType;
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
    public PageVo<UserListVo> getUsersByPage(UserQueryParam param) {
        Page<User> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
            .eq(User::getUserType, UserType.PORTAL.getValue()); // 只查询门户用户

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
        List<UserListVo> userListVos = userPage.getRecords().stream()
            .map(user -> new UserListVo(
                        user.getId(),
                        user.getUsername(),
                        DesensitizedUtil.email(user.getEmail()),
                        DesensitizedUtil.mobilePhone(user.getPhone()),
                        user.getUserType(),
                        user.getEnabled(),
                        user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null
                ))
                .collect(Collectors.toList());

        return PageVo.<UserListVo>builder()
                .pageNum(userPage.getCurrent())
                .pageSize(userPage.getSize())
                .total(userPage.getTotal())
            .records(userListVos)
                .build();
    }

    /**
     * 根据用户ID获取用户详细信息
     */
    public UserDetailVo getUserDetailById(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return null;
        }

        List<UserDetailVo.RoleVo> roleVos = new ArrayList<>();
        List<UserDetailVo.PermissionVo> permissionVos = new ArrayList<>();

        // 2. 查询用户的角色ID列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId())
        );

        List<Long> roleIds = userRoles.stream()
            .map(UserRole::getRoleId)
            .collect(Collectors.toList());

        if (!roleIds.isEmpty()) {
            List<Role> roles = roleMapper.selectByIds(roleIds);
            roleVos = roles.stream()
                .filter(role -> Boolean.TRUE.equals(role.getEnabled()))
                .map(role -> new UserDetailVo.RoleVo(
                    role.getId(),
                    role.getRoleName(),
                    role.getRoleCode(),
                    role.getRoleType(),
                    role.getDescription()
                ))
                .collect(Collectors.toList());
        }

        List<RolePermission> rolePermissions = roleIds.isEmpty() ? new ArrayList<>() : rolePermissionMapper.selectList(
            new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getRoleId, roleIds)
        );

        List<Long> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId)
            .distinct()
            .collect(Collectors.toList());

        if (!permissionIds.isEmpty()) {
            List<Permission> permissionsEntity = permissionMapper.selectByIds(permissionIds);
            permissionVos = permissionsEntity.stream()
                .filter(permission -> Boolean.TRUE.equals(permission.getEnabled()))
                .map(permission -> new UserDetailVo.PermissionVo(
                    permission.getId(),
                    permission.getPermissionName(),
                    permission.getPermissionCode(),
                    permission.getPermissionType(),
                    permission.getModulePath(),
                    permission.getDescription()
                ))
                .collect(Collectors.toList());
        }

        return new UserDetailVo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone(),
            user.getUserType(),
            user.getEnabled(),
            roleVos,
            permissionVos
        );
    }
}

