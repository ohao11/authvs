package org.max.authvs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.max.authvs.api.dto.auth.DeviceType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private static final String SECRET = "super-secret-key-change-me-please-32-bytes-minimum";
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1h
    private static final String REVOKED_PREFIX = "auth:revoked:";
    private static final String USER_DEVICE_PREFIX = "auth:user:";
    private static final String DEVICE_TYPE_CLAIM = "deviceType";

    private final StringRedisTemplate redisTemplate;

    public JwtService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token，包含设备类型信息
     *
     * @param userDetails 用户详情
     * @param deviceType  设备类型
     * @param userId      用户ID
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails, DeviceType deviceType, Long userId) {
        // 先撤销该用户在同一设备类型上的旧 token
        revokeUserDeviceToken(userId, deviceType);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .addClaims(Map.of(DEVICE_TYPE_CLAIM, deviceType.getCode()))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        // 保存新的 token 到 Redis，用于单设备登录控制
        saveUserDeviceToken(userId, deviceType, token, EXPIRATION_MS);

        return token;
    }

    /**
     * 保存用户设备 token 到 Redis
     */
    private void saveUserDeviceToken(Long userId, DeviceType deviceType, String token, long ttlMillis) {
        String key = USER_DEVICE_PREFIX + userId + ":device:" + deviceType.getCode();
        redisTemplate.opsForValue().set(key, token, ttlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 撤销用户在指定设备类型上的旧 token
     */
    private void revokeUserDeviceToken(Long userId, DeviceType deviceType) {
        String key = USER_DEVICE_PREFIX + userId + ":device:" + deviceType.getCode();
        String oldToken = redisTemplate.opsForValue().get(key);
        if (oldToken != null && !oldToken.isEmpty()) {
            revokeToken(oldToken);
            redisTemplate.delete(key);
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Jws<Claims> claims = parseClaims(token);
            String username = claims.getBody().getSubject();
            Date expiration = claims.getBody().getExpiration();
            return username.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Mark a token as revoked until its natural expiration
    public void revokeToken(String token) {
        try {
            Date expiration = parseClaims(token).getBody().getExpiration();
            if (expiration == null) return;
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            if (ttlMillis <= 0) return;
            String key = REVOKED_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            // 无法解析令牌时，忽略撤销请求
        }
    }

    // Check if token is revoked (and not yet expired). Clean up expired entries lazily
    public boolean isTokenRevoked(String token) {
        String key = REVOKED_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }
}