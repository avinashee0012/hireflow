package com.avinashee0012.hireflow.service;

import org.springframework.data.domain.Page;

import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;

public interface AdminService {
    Page<UserResponseDto> getAllUsers(int page);

    void activateUser(Long userId);

    void deactivateUser(Long userId);

    UserResponseDto updateRoles(Long userId, UpdateUserRolesRequestDto request);
}