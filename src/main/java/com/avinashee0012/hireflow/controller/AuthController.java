package com.avinashee0012.hireflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.UserLoginRequestDto;
import com.avinashee0012.hireflow.dto.request.UserRegisterRequestDto;
import com.avinashee0012.hireflow.dto.response.JwtTokenResponseDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDto> register(@Valid @RequestBody UserLoginRequestDto request){
        return ResponseEntity.status(HttpStatus.OK).body(userService.loginUser(request));
    }
}
