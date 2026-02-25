package com.avinashee0012.hireflow.service.impl;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
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
    private final CurrentUserProvider currentUserProvider;

    @Override public Page<UserResponseDto> getAllUsers(int page){
        User currentUser = getAuthorizedUser();

        if (currentUser.hasRole("ORGADMIN")){
            return userRepo.findByOrganisationId(currentUser.getOrganisationId(), PageRequest.of(page, 10))
                    .map(userMapper::toResponse);
        }

        return userRepo.findAll(PageRequest.of(page, 10)).map(userMapper::toResponse);
    }

    @Override public void activateUser(Long userId){
        User currentUser = getAuthorizedUser();
        User targetUser = getTargetUser(userId);

        validateSelfAction(currentUser, targetUser);
        validateOrgScope(currentUser, targetUser);

        targetUser.activate();
    }

    @Override public void deactivateUser(Long userId){
        User currentUser = getAuthorizedUser();
        User targetUser = getTargetUser(userId);

        validateSelfAction(currentUser, targetUser);
        validateOrgScope(currentUser, targetUser);

        targetUser.deactivate();
    }

    @Override public UserResponseDto updateRoles(Long userId, UpdateUserRolesRequestDto request){
        User currentUser = getAuthorizedUser();
        User targetUser = getTargetUser(userId);

        validateOrgScope(currentUser, targetUser);
        validateSelfRoleMutation(currentUser, targetUser, request);

        Set<Role> roles = request.getRoles().stream().map(roleName -> roleRepo.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"))).collect(Collectors.toSet());

        validateRoleEscalation(currentUser, roles);

        targetUser.getRoles().clear();
        roles.forEach(role -> targetUser.assignRole(role));

        return userMapper.toResponse(targetUser);
    }

    // HELPER METHODS
    private User getAuthorizedUser(){
        User user = currentUserProvider.getAuthenticatedUser();
        boolean allowed = user.hasRole("SUPPORT") || user.hasRole("ORGADMIN");
        if (!allowed)
            throw new CustomUnauthorizedException("Restricted endpoint. Please contact ORGADMIN for help.");
        return user;
    }

    private User getTargetUser(Long userId){
        return userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void validateSelfAction(User currentUser, User targetUser){
        if (currentUser.equals(targetUser))
            throw new CustomUnauthorizedEntityActionException("Users cannot take administrative action on themselves.");
    }

    private void validateOrgScope(User currentUser, User targetUser){
        if (currentUser.hasRole("ORGADMIN")){
            if (!Objects.equals(currentUser.getOrganisationId(), targetUser.getOrganisationId()))
                throw new CustomUnauthorizedEntityActionException("Cannot manage users from another organisation.");
        }
    }

    private void validateRoleEscalation(User currentUser, Set<Role> newRoles){
        if (currentUser.hasRole("ORGADMIN")){
            boolean containsSupport = newRoles.stream().anyMatch(role -> role.getName().equals("SUPPORT"));
            if (containsSupport) throw new CustomUnauthorizedEntityActionException("ORGADMIN cannot assign SUPPORT role.");
        }
    }

    private void validateSelfRoleMutation(User currentUser, User targetUser, UpdateUserRolesRequestDto request){
        if (currentUser.equals(targetUser)){
            boolean removingSupport = currentUser.hasRole("SUPPORT") && !request.getRoles().contains("SUPPORT");
            if (removingSupport) throw new CustomUnauthorizedEntityActionException("Cannot remove your own SUPPORT role.");
        }
    }
}
