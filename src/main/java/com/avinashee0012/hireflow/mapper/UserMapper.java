package com.avinashee0012.hireflow.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;

@Component
public class UserMapper {

    public UserResponseDto toResponse(User user){
        Set<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());
        return new UserResponseDto(user.getId(), user.getEmail(), roles, user.isActive(), user.getOrganisationId());
    }
}
