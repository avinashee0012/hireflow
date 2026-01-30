package com.avinashee0012.hireflow.dto.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenResponseDto {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String email;
    private Set<String> roles;
}
