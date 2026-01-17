package org.max.authvs.api;

import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.config.I18nMessageService;
import org.max.authvs.api.exception.InvalidTokenException;
import org.max.authvs.api.exception.TokenRevokedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final I18nMessageService i18nMessageService;

    public GlobalExceptionHandler(I18nMessageService i18nMessageService) {
        this.i18nMessageService = i18nMessageService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultDTO<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.BAD_REQUEST, message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResultDTO<Void>> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String localizedMessage = i18nMessageService.getMessage("auth.unauthorized");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.UNAUTHORIZED, localizedMessage));
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ResultDTO<Void>> handleTokenRevoked(TokenRevokedException ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String localizedMessage = i18nMessageService.getMessage("auth.token.revoked");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.UNAUTHORIZED, localizedMessage));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ResultDTO<Void>> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String localizedMessage = i18nMessageService.getMessage("auth.token.invalid");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.UNAUTHORIZED, localizedMessage));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultDTO<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String localizedMessage = i18nMessageService.getMessage("auth.forbidden");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.FORBIDDEN, localizedMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDTO<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        logErrorWithRequest(ex, request);
        String localizedMessage = i18nMessageService.getMessage("exception.internal.error");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.SERVER_ERROR, localizedMessage));
    }

    private void logErrorWithRequest(Exception ex, HttpServletRequest request) {
        String body = extractBody(request);
        String headers = extractHeaders(request);
        String clientIp = getClientIp(request);
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String protocol = request.getProtocol();
        
        // 按照标准HTTP请求格式打印
        StringBuilder httpRequest = new StringBuilder("\n");
        httpRequest.append("==================== HTTP Request ====================\n");
        httpRequest.append(String.format("%s %s%s %s\n", 
                request.getMethod(), 
                request.getRequestURI(), 
                queryString, 
                protocol));
        httpRequest.append(headers);
        if (!body.equals("<no body cached>")) {
            httpRequest.append("\n").append(body);
        }
        httpRequest.append("\n==================== Client Info ====================\n");
        httpRequest.append(String.format("Client IP: %s\n", clientIp));
        httpRequest.append("==================== Exception ====================\n");
        httpRequest.append(String.format("Message: %s\n", ex.getMessage()));
        httpRequest.append("======================================================");
        
        log.error(httpRequest.toString(), ex);
    }

    private String extractBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    wrapper.getCharacterEncoding();
                    return new String(buf, wrapper.getCharacterEncoding());
                } catch (Exception ignored) {
                    return "<unable to decode body>";
                }
            }
        }
        return "<no body cached>";
    }

    private String extractHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 敏感信息脱敏
            if ("Authorization".equalsIgnoreCase(headerName) && headerValue != null && headerValue.length() > 20) {
                headerValue = headerValue.substring(0, 20) + "...";
            }
            headers.append(headerName).append(": ").append(headerValue).append("\n");
        }
        return !headers.isEmpty() ? headers.toString() : "";
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }
}