package org.max.authvs.api;

import org.max.authvs.api.dto.ResultDTO;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultDTO<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.BAD_REQUEST, message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResultDTO<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResultDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultDTO<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.ok(ResultDTO.error(ResultDTO.SERVER_ERROR, ex.getMessage()));
    }
}