package org.max.authvs.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.max.authvs.entity.*;
import org.max.authvs.mapper.*;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.security.PermissionCacheService;
import org.max.authvs.security.PermissionCacheVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final PermissionCacheService permissionCacheService;

    public CustomUserDetailsService(UserMapper userMapper,
                                    UserRoleMapper userRoleMapper,
                                    RoleMapper roleMapper,
                                    RolePermissionMapper rolePermissionMapper,
                                    PermissionMapper permissionMapper,
                                    PermissionCacheService permissionCacheService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        // 默认场景：已认证用户加载权限，使用缓存提高性能
        return loadUserByUsername(username, true);
    }

    /**
     * 加载用户详情，支持缓存控制
     *
     * @param username 用户名
     * @param useCache 是否使用缓存：
     *                 - true: 优先使用缓存（适用于已认证用户），如缓存不存在则查询数据库但不包含密码
     *                 - false: 绕过缓存直接查询数据库（适用于登录认证，需要验证密码）
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    public UserDetails loadUserByUsername(@NonNull String username, boolean useCache) throws UsernameNotFoundException {
        // 0. 如果使用缓存，优先从缓存读取，避免数据库查询
        if (useCache) {
            Optional<PermissionCacheVO> cacheOpt = permissionCacheService.getByUsername(username);
            if (cacheOpt.isPresent()) {
                log.info("Permission cache hit for user: {}", username);
                return buildFromCache(cacheOpt.get());
            }
        }

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

        CustomUserDetails details = new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getUserType(),
                Boolean.TRUE.equals(user.getEnabled()),
                new ArrayList<>(authorities)
        );

        // 缓存权限，后续请求直接命中缓存
        permissionCacheService.cachePermissions(details);

        return details;
    }

    private CustomUserDetails buildFromCache(PermissionCacheVO cache) {
        Set<GrantedAuthority> authorities = cache.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new CustomUserDetails(
                cache.getUserId(),
                cache.getUsername(),
                "", // 密码不存储在缓存中
                cache.getEmail(),
                cache.getPhone(),
                cache.getUserType(),
                Boolean.TRUE.equals(cache.getEnabled()),
                new ArrayList<>(authorities)
        );
    }
}
