package com.avinashee0012.hireflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Domain Exceptions
    @ExceptionHandler(CustomDuplicateEntityException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomDuplicateEntityException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("Duplicate entity: {} [{} {}] - {}", status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    @ExceptionHandler(CustomUnauthorizedEntityActionException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomUnauthorizedEntityActionException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        log.warn("Unauthorized action: {} [{} {}] - {}", status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFoundException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Entity not found: {} [{} {}] - {}", status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    // Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("Validation failed: {} [{} {}] - {}", status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    // Auth & Security Errors
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        log.warn("Authentication failed: {} [{} {}]", status.value(), request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    @ExceptionHandler(CustomInactiveUserException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomInactiveUserException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.FORBIDDEN;
        log.warn("Inactive user access attempt: {} [{} {}] - {}", status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalStateException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Illegal system state: {} [{} {}]", status.value(), request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

    // Generic Errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unhandled exception: {} [{} {}]", status.value(), request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, ex, request));
    }

}
