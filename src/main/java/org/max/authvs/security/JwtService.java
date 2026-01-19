package org.max.authvs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private static final String SECRET = "super-secret-key-change-me-please-32-bytes-minimum";
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1h
    private static final String REVOKED_PREFIX = "auth:revoked:";

    private final StringRedisTemplate redisTemplate;

    public JwtService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
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