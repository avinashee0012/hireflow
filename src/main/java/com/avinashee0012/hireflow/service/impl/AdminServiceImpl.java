package com.avinashee0012.hireflow.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.mapper.UserMapper;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.AdminService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService{

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final UserMapper userMapper;

    @Override public Page<UserResponseDto> getAllUsers(int page){
        return userRepo.findAll(PageRequest.of(page, 10)).map(userMapper::toResponse);
    }

    @Override public void activateUser(Long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.activate();
    }

    @Override public void deactivateUser(Long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.deactivate();
    }

    @Override public UserResponseDto updateRoles(Long userId, UpdateUserRolesRequestDto request){
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        Set<Role> roles = request.getRoles().stream().map(roleName -> roleRepo.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"))).collect(Collectors.toSet());

        roles.forEach(role -> user.assignRole(role));
        return userMapper.toResponse(user);
    }

}
