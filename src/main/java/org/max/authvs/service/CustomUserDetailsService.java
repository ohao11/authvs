package org.max.authvs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.max.authvs.entity.*;
import org.max.authvs.mapper.*;
import org.max.authvs.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务，从数据库加载用户信息（MyBatis-Plus + RBAC）
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public CustomUserDetailsService(UserMapper userMapper,
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

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        // 1. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 2. 查询用户的角色ID列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId())
        );

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            log.warn("User {} has no roles assigned", username);
            // 用户没有角色，返回空权限列表
            return new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getUserType(),
                    Boolean.TRUE.equals(user.getEnabled()),
                    new ArrayList<>()
            );
        }

        // 3. 查询角色详情
        List<Role> roles = roleMapper.selectByIds(roleIds);
        log.info("User {} found roles: {}", username,
                roles.stream().map(r -> r.getRoleCode() + "(" + r.getEnabled() + ")").collect(Collectors.toList()));

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
        List<Permission> permissions = new ArrayList<>();
        if (!permissionIds.isEmpty()) {
            permissions = permissionMapper.selectByIds(permissionIds);
        }

        // 6. 构建Spring Security的权限列表
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 添加角色权限：ROLE_角色编码
        for (Role role : roles) {
            if (Boolean.TRUE.equals(role.getEnabled())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
            }
        }

        // 添加权限：PERM_权限编码
        for (Permission permission : permissions) {
            if (Boolean.TRUE.equals(permission.getEnabled())) {
                authorities.add(new SimpleGrantedAuthority("PERM_" + permission.getPermissionCode()));
            }
        }

        log.info("User {} loaded with authorities: {}",
                username,
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getUserType(),
                Boolean.TRUE.equals(user.getEnabled()),
                new ArrayList<>(authorities)
        );
    }
}
