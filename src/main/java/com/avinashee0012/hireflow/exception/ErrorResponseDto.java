package com.avinashee0012.hireflow.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    ErrorResponseDto(HttpStatus httpStatus, Exception ex, HttpServletRequest request){
        status = httpStatus.value();
        error = httpStatus.name();
        message = ex.getMessage();
        path = request.getMethod() + " " + request.getRequestURI();
        timestamp = LocalDateTime.now();
    }
}
