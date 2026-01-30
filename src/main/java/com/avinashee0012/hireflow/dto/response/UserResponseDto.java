package com.avinashee0012.hireflow.dto.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private Set<String> roles;
    private boolean active;
    private Long organisationId;
}