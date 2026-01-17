package org.max.authvs.api;

import org.max.authvs.api.dto.ResultDTO;
import org.max.authvs.config.I18nMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nMessageService i18nMessageService;

    public GlobalExceptionHandler(I18nMessageService i18nMessageService) {
        this.i18nMessageService = i18nMessageService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultDTO<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        String localizedMessage = i18nMessageService.getMessage("exception.bad.request");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.BAD_REQUEST, localizedMessage));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResultDTO<Void>> handleAuth(AuthenticationException ex) {
        String localizedMessage = i18nMessageService.getMessage("auth.unauthorized");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.UNAUTHORIZED, localizedMessage));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        String localizedMessage = i18nMessageService.getMessage("auth.forbidden");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.FORBIDDEN, localizedMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDTO<Void>> handleGeneric(Exception ex) {
        String localizedMessage = i18nMessageService.getMessage("exception.internal.error");
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.SERVER_ERROR, localizedMessage));
    }
}