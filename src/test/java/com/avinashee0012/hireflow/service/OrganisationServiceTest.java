package com.avinashee0012.hireflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

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
import com.avinashee0012.hireflow.domain.enums.OrganisationStatus;
import com.avinashee0012.hireflow.dto.request.OrganisationRequestDto;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.OrganisationMapper;
import com.avinashee0012.hireflow.repository.OrganisationRepo;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.impl.OrganisationServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class OrganisationServiceTest{

    @Mock
    private OrganisationRepo organisationRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private RoleRepo roleRepo;
    @Mock
    private OrganisationMapper organisationMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private OrganisationServiceImpl organisationService;

    private OrganisationRequestDto request;
    private User supportUser;
    private User orgAdminUser;
    private User recruiterUser;
    private Long orgId;
    private Role orgAdminRole;
    private Role supportRole;
    private Role recruiterRole;
    private Organisation organisation;

    @BeforeEach void setup(){
        orgAdminRole = new Role("ORGADMIN");
        supportRole = new Role("SUPPORT");
        recruiterRole = new Role("RECRUITER");

        supportUser = new User("support@email.com", "encryptedPass", null);
        supportUser.assignRole(supportRole);
        ReflectionTestUtils.setField(supportUser, "id", 1L);

        orgAdminUser = new User("orgadmin@email.com", "encryptedPass", null);
        orgAdminUser.assignRole(orgAdminRole);
        ReflectionTestUtils.setField(orgAdminUser, "id", 2L);

        recruiterUser = new User("recruiter@email.com", "encryptedPass", null);
        recruiterUser.assignRole(recruiterRole);
        ReflectionTestUtils.setField(recruiterUser, "id", 3L);

        request = new OrganisationRequestDto();
        String orgName = "TestOrg";
        request.setName(orgName);
        request.setAdminUserId(orgAdminUser.getId());

        orgId = 1L;
        organisation = new Organisation(orgName, orgAdminUser.getId());
        ReflectionTestUtils.setField(organisation, "id", orgId);
    }

    @Test void shouldCreateOrganisationSuccessfully(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(organisationRepo.existsByName(request.getName())).thenReturn(false);
        when(userRepo.findById(request.getAdminUserId())).thenReturn(Optional.of(orgAdminUser));
        when(organisationRepo.save(any(Organisation.class))).thenReturn(organisation);
        when(organisationMapper.toResponse(any(Organisation.class))).thenReturn(new OrganisationResponseDto(
                organisation.getId(), organisation.getName(), OrganisationStatus.ACTIVE, orgAdminUser.getId()));

        OrganisationResponseDto response = organisationService.createOrganisation(request);

        assertNotNull(response);
        assertEquals("TestOrg", response.getName());
        assertEquals(1L, orgAdminUser.getOrganisationId());
        assertEquals(2L, response.getAdminUserId());

        verify(organisationRepo).save(any());
    }

    @Test void shouldSuspendOrganisation(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(organisationRepo.findById(orgId)).thenReturn(Optional.of(organisation));
        organisationService.suspendOrganisation(orgId);

        assertEquals(OrganisationStatus.SUSPENDED, organisation.getStatus());

        verify(organisationRepo).findById(orgId);
    }

    @Test void shouldThrowIfSuspendNotSupport(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);

        assertThrows(CustomUnauthorizedException.class, () -> organisationService.suspendOrganisation(orgId));

        verify(organisationRepo, never()).findById(any());
    }

    @Test void shouldThrowIfOrganisationNotFoundOnSuspend(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);

        when(organisationRepo.findById(orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> organisationService.suspendOrganisation(orgId));
    }

    @Test void shouldActivateOrganisation(){
        organisation.suspend(); // start from suspended state

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(organisationRepo.findById(orgId)).thenReturn(Optional.of(organisation));

        organisationService.activateOrganisation(orgId);

        assertEquals(OrganisationStatus.ACTIVE, organisation.getStatus());
    }

    @Test void shouldReturnOrganisationForSupport(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);
        when(organisationRepo.findById(orgId)).thenReturn(Optional.of(organisation));
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponseDto(orgId,
                organisation.getName(), OrganisationStatus.ACTIVE, orgAdminUser.getId()));

        OrganisationResponseDto response = organisationService.getOrganisation(orgId);

        assertNotNull(response);
        assertEquals("TestOrg", response.getName());
    }

    @Test void shouldReturnOrganisationForOwnOrgAdmin(){
        orgAdminUser.assignOrganisation(1L);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        when(organisationRepo.findById(orgId)).thenReturn(Optional.of(organisation));
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponseDto(orgId,
                organisation.getName(), OrganisationStatus.ACTIVE, orgAdminUser.getId()));

        OrganisationResponseDto response = organisationService.getOrganisation(orgId);

        assertNotNull(response);
    }

    @Test void shouldThrowIfOrgAdminAccessingOtherOrganisation(){
        orgAdminUser.assignOrganisation(99L); // different org

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        when(organisationRepo.findById(orgId)).thenReturn(Optional.of(organisation));

        assertThrows(CustomUnauthorizedException.class, () -> organisationService.getOrganisation(orgId));
    }
    
}
