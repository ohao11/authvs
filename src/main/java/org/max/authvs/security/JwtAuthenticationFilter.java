package org.max.authvs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.max.authvs.config.I18nMessageService;
import org.max.authvs.entity.OperationLog;
import org.max.authvs.exception.InvalidTokenException;
import org.max.authvs.exception.TokenRevokedException;
import org.max.authvs.service.OperationLogService;
import org.max.authvs.utils.IpUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final I18nMessageService i18nMessageService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final OperationLogService operationLogService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   I18nMessageService i18nMessageService,
                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
                                   OperationLogService operationLogService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.i18nMessageService = i18nMessageService;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.operationLogService = operationLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            // If token has been revoked, reject immediately
            if (jwtService.isTokenRevoked(token)) {
                String message = i18nMessageService.getMessage("auth.token.revoked");
                // 记录操作日志（失败）
                recordAuthFailureLog(request, token, message);
                handlerExceptionResolver.resolveException(request, response, null, new TokenRevokedException(message));
                return;
            }
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            String message = i18nMessageService.getMessage("auth.token.invalid");
            // 记录操作日志（失败）
            recordAuthFailureLog(request, token, message);
            handlerExceptionResolver.resolveException(request, response, null, new InvalidTokenException(message));
        }
    }

    private void recordAuthFailureLog(HttpServletRequest request, String token, String message) {
        try {
            OperationLog log = new OperationLog();
            log.setOperationType("LOGIN");
            log.setOperationModule("认证");
            log.setOperationDesc(message);
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(IpUtils.getRealIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            // 尝试从token中提取用户名和设备类型
            try {
                String username = jwtService.extractUsername(token);
                String deviceCode = jwtService.extractDeviceTypeCode(token);
                Long userId = jwtService.extractUserId(token);
                log.setUsername(username);
                log.setDeviceType(deviceCode);
                if (userId != null) {
                    log.setUserId(userId);
                }
            } catch (Exception ignored) {
            }
            log.setStatus("FAIL");
            log.setErrorMessage(message);
            log.setExecuteTime(0L);
            log.setCreatedAt(java.time.LocalDateTime.now());
            operationLogService.saveLog(log);
        } catch (Exception ignored) {
            // 忽略日志记录中的异常，避免影响主流程
        }
    }
}