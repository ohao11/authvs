package org.max.authvs.service;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.admin.in.AdminCreateParam;
import org.max.authvs.api.dto.admin.in.AdminQueryParam;
import org.max.authvs.api.dto.admin.in.AdminUpdateParam;
import org.max.authvs.api.dto.admin.out.AdminDetailVo;
import org.max.authvs.api.dto.admin.out.AdminListVo;
import org.max.authvs.api.dto.admin.out.RoleListVo;
import org.max.authvs.entity.Role;
import org.max.authvs.entity.RolePermission;
import org.max.authvs.entity.User;
import org.max.authvs.entity.UserRole;
import org.max.authvs.enums.RoleType;
import org.max.authvs.enums.UserType;
import org.max.authvs.mapper.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询后台管理员
     */
    public PageVo<AdminListVo> getAdminsByPage(AdminQueryParam param) {
        Page<User> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUserType, UserType.ADMIN.getValue());

        if (StrUtil.isNotBlank(param.getUsername())) {
            wrapper.like(User::getUsername, param.getUsername());
        }
        if (StrUtil.isNotBlank(param.getEmail())) {
            wrapper.like(User::getEmail, param.getEmail());
        }
        if (StrUtil.isNotBlank(param.getPhone())) {
            wrapper.like(User::getPhone, param.getPhone());
        }
        if (param.getEnabled() != null) {
            wrapper.eq(User::getEnabled, param.getEnabled());
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<Long> userIds = userPage.getRecords().stream().map(User::getId).toList();

        Map<Long, List<AdminListVo.RoleVo>> userRoleMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<UserRole> userRoles = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds));
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().toList();
            if (!roleIds.isEmpty()) {
                Map<Long, Role> roleMap = roleMapper.selectByIds(roleIds).stream()
                        .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                        .collect(Collectors.toMap(Role::getId, r -> r, (a, b) -> a));

                userRoleMap.putAll(userRoles.stream()
                        .filter(ur -> roleMap.containsKey(ur.getRoleId()))
                        .collect(Collectors.groupingBy(UserRole::getUserId,
                                Collectors.mapping(ur -> {
                                    Role role = roleMap.get(ur.getRoleId());
                                    return new AdminListVo.RoleVo(
                                            role.getId(),
                                            role.getRoleName(),
                                            role.getRoleCode()
                                    );
                                }, Collectors.toList()))));
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<AdminListVo> records = userPage.getRecords().stream()
                .map(u -> new AdminListVo(
                        u.getId(),
                        u.getUsername(),
                        DesensitizedUtil.email(u.getEmail()),
                        DesensitizedUtil.mobilePhone(u.getPhone()),
                        Boolean.TRUE.equals(u.getEnabled()) ? 1 : 0,
                        u.getCreatedAt() != null ? u.getCreatedAt().format(formatter) : null,
                        userRoleMap.getOrDefault(u.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());

        return PageVo.<AdminListVo>builder()
                .pageNum(userPage.getCurrent())
                .pageSize(userPage.getSize())
                .total(userPage.getTotal())
                .records(records)
                .build();
    }

    /**
     * 获取管理员详情
     */
    public AdminDetailVo getAdminDetail(Long adminId) {
        User user = userMapper.selectById(adminId);
        if (user == null || !Objects.equals(user.getUserType(), UserType.ADMIN.getValue())) {
            return null;
        }

        // roles
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<AdminDetailVo.RoleVo> roles = new ArrayList<>();
        List<AdminDetailVo.PermissionVo> permissions = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            roles = roleMapper.selectByIds(roleIds).stream()
                    .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                    .map(r -> new AdminDetailVo.RoleVo(r.getId(), r.getRoleName(), r.getRoleCode()))
                    .collect(Collectors.toList());

            List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds));
            List<Long> permIds = rolePermissions.stream().map(RolePermission::getPermissionId).distinct().toList();
            if (!permIds.isEmpty()) {
                permissions = permissionMapper.selectByIds(permIds).stream()
                        .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
                        .map(p -> new AdminDetailVo.PermissionVo(p.getId(), p.getPermissionName(), p.getPermissionCode()))
                        .collect(Collectors.toList());
            }
        }

        return new AdminDetailVo(
                user.getId(),
                user.getUsername(),
                DesensitizedUtil.email(user.getEmail()),
                DesensitizedUtil.mobilePhone(user.getPhone()),
                user.getEnabled(),
                roles,
                permissions
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createAdmin(AdminCreateParam param) {
        // 唯一性校验
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, param.getUsername()));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(param.getUsername());
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        user.setEmail(param.getEmail());
        user.setPhone(param.getPhone());
        user.setUserType(UserType.ADMIN.getValue());
        user.setEnabled(param.getEnabled());
        userMapper.insert(user);

        saveUserRoles(user.getId(), param.getRoleIds());
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(AdminUpdateParam param) {
        User user = userMapper.selectById(param.getId());
        if (user == null || !Objects.equals(user.getUserType(), UserType.ADMIN.getValue())) {
            throw new IllegalArgumentException("管理员不存在");
        }

        if (StrUtil.isNotBlank(param.getPassword())) {
            user.setPassword(passwordEncoder.encode(param.getPassword()));
        }
        if (param.getEmail() != null) {
            user.setEmail(param.getEmail());
        }
        if (param.getPhone() != null) {
            user.setPhone(param.getPhone());
        }
        if (param.getEnabled() != null) {
            user.setEnabled(param.getEnabled());
        }
        userMapper.updateById(user);

        // 重置角色
        if (param.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
            saveUserRoles(user.getId(), param.getRoleIds());
        }
        return true;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    /**
     * 获取所有可用的后台管理员角色列表（用于创建/编辑管理员时分配）
     * 只返回后台管理员角色，且不包括超级管理员
     */
    public List<RoleListVo> getRolesList() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getEnabled, true)
                        .eq(Role::getRoleType, RoleType.ADMIN.getValue())
                        .ne(Role::getRoleCode, "SUPER_ADMIN")
                        .orderByAsc(Role::getId)
        );
        return roles.stream()
                .map(r -> new RoleListVo(r.getId(), r.getRoleName(), r.getRoleCode(), r.getRoleType(), r.getDescription(), r.getEnabled()))
                .collect(Collectors.toList());
    }
}