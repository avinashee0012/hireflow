package com.avinashee0012.hireflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.UserMapper;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.impl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest{

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User supportUser;
    private User orgAdminUser;
    private User candidateUser;

    private Role supportRole;
    private Role orgAdminRole;
    private Role candidateRole;

    private Organisation organisation;

    @BeforeEach void setup(){
        supportRole = new Role("SUPPORT");
        orgAdminRole = new Role("ORGADMIN");
        candidateRole = new Role("CANDIDATE");

        supportUser = new User("support@email.com", "encryptedPass", null);
        supportUser.assignRole(supportRole);
        ReflectionTestUtils.setField(supportUser, "id", 1L);

        orgAdminUser = new User("orgadmin@email.com", "encryptedPass", 10L);
        orgAdminUser.assignRole(orgAdminRole);
        ReflectionTestUtils.setField(orgAdminUser, "id", 2L);

        organisation = new Organisation("TestOrg", 2L);
        ReflectionTestUtils.setField(organisation, "id", 10L);

        candidateUser = new User("candidate@email.com", "encryptedPass", null);
        candidateUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(candidateUser, "id", 3L);

    }

    @Test void shouldThrowWhenUserNotSupportOrOrgAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);

        assertThrows(CustomUnauthorizedException.class, () -> adminService.getAllUsers(0));
    }

    @Test void orgAdminShouldNotManageOtherOrganisationUsers(){
        User targetUser = new User("other@email.com", "encryptedPass", 11L);
        targetUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(targetUser, "id", 5L);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        when(userRepo.findById(5L)).thenReturn(Optional.of(targetUser));

        assertThrows(CustomUnauthorizedEntityActionException.class, () -> adminService.activateUser(5L));
    }

    @Test void shouldNotAllowSelfDeactivation(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(userRepo.findById(1L)).thenReturn(Optional.of(supportUser));

        assertThrows(CustomUnauthorizedEntityActionException.class, () -> adminService.deactivateUser(1L));
    }

    @Test void orgAdminShouldNotAssignSupportRole(){
        User targetUser = new User("candidate2@email.com", "encryptedPass", 10L);
        targetUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(targetUser, "id", 5L);

        when(roleRepo.findByName("SUPPORT")).thenReturn(Optional.of(supportRole));

        UpdateUserRolesRequestDto request = new UpdateUserRolesRequestDto();
        request.setRoles(Set.of("SUPPORT"));

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        when(userRepo.findById(5L)).thenReturn(Optional.of(targetUser));

        assertThrows(CustomUnauthorizedEntityActionException.class, () -> adminService.updateRoles(5L, request));
    }

    @Test void supportShouldUpdateRolesSuccessfully(){
        User targetUser = new User("candidate@email.com", "encryptedPass", 10L);
        targetUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(targetUser, "id", 5L);

        Role recruiterRole = new Role("RECRUITER");

        UpdateUserRolesRequestDto request = new UpdateUserRolesRequestDto();
        request.setRoles(Set.of("RECRUITER"));

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(userRepo.findById(5L)).thenReturn(Optional.of(targetUser));
        when(roleRepo.findByName("RECRUITER")).thenReturn(Optional.of(recruiterRole));
        when(userMapper.toResponse(targetUser))
                .thenReturn(new UserResponseDto(5L, "candidate@email.com", Set.of("RECRUITER"), true, 10L));

        UserResponseDto response = adminService.updateRoles(5L, request);

        assertTrue(response.getRoles().contains("RECRUITER"));
    }

    @Test void supportCanActivateUserFromAnyOrganisation(){
        User targetUser = new User("external@email.com", "encryptedPass", 11L);
        targetUser.assignRole(candidateRole);
        targetUser.deactivate();
        ReflectionTestUtils.setField(targetUser, "id", 5L);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(userRepo.findById(5L)).thenReturn(Optional.of(targetUser));

        adminService.activateUser(5L);

        assertTrue(targetUser.isActive());
    }

}
