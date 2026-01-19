package org.max.authvs.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限缓存服务：
 * - 登录时缓存权限
 * - 请求时优先命中缓存，减少数据库查询
 * - 权限变更时清理缓存
 */
@Service
@Slf4j
public class PermissionCacheService {

    private static final String KEY_PREFIX = "auth:permissions:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1); // 与 JWT 有效期保持一致

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PermissionCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<PermissionCacheVO> getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + username;
        return readFromRedis(key);
    }

    public void cachePermissions(CustomUserDetails details) {
        if (details == null || details.getId() == null) {
            return;
        }
        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PermissionCacheVO cacheVO = new PermissionCacheVO(
                details.getId(),
                details.getUsername(),
                details.getEmail(),
                details.getPhone(),
                details.getUserType(),
                details.isEnabled(),
                authorities,
                System.currentTimeMillis()
        );
        try {
            String json = objectMapper.writeValueAsString(cacheVO);
            if (StringUtils.hasText(details.getUsername())) {
                String key = KEY_PREFIX + details.getUsername();
                redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize permission cache for userId={}", details.getId(), e);
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, skip caching permissions for userId={}", details.getId(), e);
        }
    }

    public void clearCache(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        try {
            String key = KEY_PREFIX + username;
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, skip clearing permission cache for username={}", username, e);
        }
    }

    private Optional<PermissionCacheVO> readFromRedis(String key) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(cached)) {
                return Optional.empty();
            }
            PermissionCacheVO vo = objectMapper.readValue(cached, PermissionCacheVO.class);
            return Optional.of(vo);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize permission cache for key={}, clearing it", key, e);
            try {
                redisTemplate.delete(key);
            } catch (DataAccessException ex) {
                log.warn("Redis unavailable while clearing corrupted cache key={}", key, ex);
            }
            return Optional.empty();
        } catch (DataAccessException e) {
            log.warn("Redis unavailable when reading cache key={}", key, e);
            return Optional.empty();
        }
    }
}
