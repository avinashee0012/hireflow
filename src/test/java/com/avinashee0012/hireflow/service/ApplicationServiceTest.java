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
import com.avinashee0012.hireflow.config.security.OrganisationAccessGuard;
import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;
import com.avinashee0012.hireflow.exception.CustomDuplicateEntityException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.ApplicationMapper;
import com.avinashee0012.hireflow.repository.ApplicationRepo;
import com.avinashee0012.hireflow.repository.JobRepo;
import com.avinashee0012.hireflow.service.impl.ApplicationServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest{

    @Mock
    private ApplicationRepo applicationRepo;
    @Mock
    private JobRepo jobRepo;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private OrganisationAccessGuard organisationAccessGuard;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Role candidateRole;
    private Role recruiterRole;
    private Role orgAdminRole;
    private Role supportRole;

    private User candidateUser;
    private User recruiterUser;
    private User orgAdminUser;
    private User supportUser;

    private Job job;
    private Organisation organisation;
    private Application application;

    private static final Long ORG_ID = 10L;
    private static final Long JOB_ID = 100L;
    private static final Long APPLICATION_ID = 1000L;

    @BeforeEach void setup(){
        recruiterRole = new Role("RECRUITER");
        orgAdminRole = new Role("ORGADMIN");
        candidateRole = new Role("CANDIDATE");
        supportRole = new Role("SUPPORT");

        recruiterUser = new User("recruiter@email.com", "encryptedPass", null);
        recruiterUser.assignRole(recruiterRole);
        ReflectionTestUtils.setField(recruiterUser, "id", 1L);

        orgAdminUser = new User("orgadmin@email.com", "encryptedPass", null);
        orgAdminUser.assignRole(orgAdminRole);
        ReflectionTestUtils.setField(orgAdminUser, "id", 2L);

        candidateUser = new User("candidate@email.com", "encryptedPass", null);
        candidateUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(candidateUser, "id", 3L);

        supportUser = new User("support@email.com", "encryptedPass", null);
        candidateUser.assignRole(supportRole);
        ReflectionTestUtils.setField(supportUser, "id", 4L);

        organisation = new Organisation("TestOrg", 2L);
        ReflectionTestUtils.setField(organisation, "id", ORG_ID);

        job = new Job("Java Developer", "Java, Spring Boot", organisation.getId(), recruiterUser.getId());
        ReflectionTestUtils.setField(job, "id", JOB_ID);

        application = new Application(JOB_ID, JOB_ID, APPLICATION_ID);
        ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
    }

    @Test void shouldApplySuccessfully(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(jobRepo.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(applicationRepo.existsByJobIdAndCandidateId(JOB_ID, candidateUser.getId())).thenReturn(false);
        when(applicationRepo.save(any(Application.class))).thenReturn(application);

        ApplicationResponseDto response = applicationService.apply(JOB_ID);

        assertNotNull(response);
        assertEquals(JOB_ID, response.getJobId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());

        verify(applicationRepo).save(any());
    }

    @Test void shouldThrowOnApplyIfUserNotCandidate(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);

        assertThrows(CustomUnauthorizedException.class, () -> applicationService.apply(JOB_ID));

        verify(jobRepo, never()).findById(any());
    }

    @Test void shouldThrowIfJobNotFound(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(jobRepo.findById(JOB_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> applicationService.apply(JOB_ID));
    }

    @Test void shouldThrowIfJobNotOpen(){
        job.close();

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(jobRepo.findById(JOB_ID)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () -> applicationService.apply(JOB_ID));
    }

    @Test void shouldThrowIfAlreadyApplied(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(jobRepo.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(applicationRepo.existsByJobIdAndCandidateId(JOB_ID, candidateUser.getId())).thenReturn(true);

        assertThrows(CustomDuplicateEntityException.class, () -> applicationService.apply(JOB_ID));

        verify(applicationRepo, never()).save(any());
    }

    @Test void shouldWithdrawApplicationSuccessfully(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(applicationRepo.findByIdAndCandidateId(APPLICATION_ID, candidateUser.getId()))
                .thenReturn(Optional.of(application));

        applicationService.withdraw(APPLICATION_ID);

        assertEquals(ApplicationStatus.WITHDRAWN, application.getApplicationStatus());

        verify(applicationRepo).findByIdAndCandidateId(APPLICATION_ID, candidateUser.getId());
    }

    @Test void shouldThrowOnWithdrawIfUserNotCandidate(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);

        assertThrows(CustomUnauthorizedException.class, () -> applicationService.withdraw(APPLICATION_ID));

        verify(applicationRepo, never()).findByIdAndCandidateId(any(), any());
    }

    @Test void shouldThrowIfApplicationNotFound(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(applicationRepo.findByIdAndCandidateId(APPLICATION_ID, candidateUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> applicationService.withdraw(APPLICATION_ID));
    }

    @Test void shouldThrowIfAlreadyWithdrawn(){
        application.withdraw(); // already withdrawn

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);
        when(applicationRepo.findByIdAndCandidateId(APPLICATION_ID, candidateUser.getId())).thenReturn(Optional.of(application));

        assertThrows(CustomUnauthorizedEntityActionException.class, () -> applicationService.withdraw(APPLICATION_ID));
    }
    
}
