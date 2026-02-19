package com.avinashee0012.hireflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.config.security.OrganisationAccessGuard;
import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;
import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
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
        when(applicationRepo.findByIdAndCandidateId(APPLICATION_ID, candidateUser.getId()))
                .thenReturn(Optional.of(application));

        assertThrows(CustomUnauthorizedEntityActionException.class, () -> applicationService.withdraw(APPLICATION_ID));
    }

    @Test void shouldThrowIfUserNotRecruiterOrOrgAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        assertThrows(CustomUnauthorizedException.class, () -> applicationService.updateStatus(APPLICATION_ID, request));
    }

    @Test void shouldUpdateStatusForOrgAdmin(){
        orgAdminUser.assignOrganisation(ORG_ID);

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(orgAdminUser);
        when(applicationRepo.findByIdAndOrganisationId(APPLICATION_ID, ORG_ID)).thenReturn(Optional.of(application));

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        when(applicationMapper.toResponse(application))
                .thenReturn(new ApplicationResponseDto(APPLICATION_ID, JOB_ID, ApplicationStatus.SHORTLISTED));

        ApplicationResponseDto response = applicationService.updateStatus(APPLICATION_ID, request);

        assertEquals(ApplicationStatus.SHORTLISTED, application.getApplicationStatus());
        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
    }

    @Test void shouldThrowIfApplicationNotInOrgForAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(orgAdminUser);
        when(applicationRepo.findByIdAndOrganisationId(APPLICATION_ID, orgAdminUser.getOrganisationId()))
                .thenReturn(Optional.empty());

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        assertThrows(CustomUnauthorizedEntityActionException.class,
                () -> applicationService.updateStatus(APPLICATION_ID, request));
    }

    @Test void shouldUpdateStatusForRecruiter(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);
        when(applicationRepo.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(jobRepo.existsByIdAndAssignedRecruiterId(JOB_ID, recruiterUser.getId())).thenReturn(true);

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.REJECTED);

        when(applicationMapper.toResponse(application))
                .thenReturn(new ApplicationResponseDto(APPLICATION_ID, JOB_ID, ApplicationStatus.REJECTED));

        ApplicationResponseDto response = applicationService.updateStatus(APPLICATION_ID, request);

        assertEquals(ApplicationStatus.REJECTED, application.getApplicationStatus());
        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
    }

    @Test void shouldThrowIfRecruiterNotAssignedToJob(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);
        when(applicationRepo.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
        when(jobRepo.existsByIdAndAssignedRecruiterId(JOB_ID, recruiterUser.getId())).thenReturn(false);

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        assertThrows(CustomUnauthorizedEntityActionException.class,
                () -> applicationService.updateStatus(APPLICATION_ID, request));
    }

    @Test void shouldThrowIfInvalidTransitionStatus(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(orgAdminUser);
        when(applicationRepo.findByIdAndOrganisationId(APPLICATION_ID, orgAdminUser.getOrganisationId()))
                .thenReturn(Optional.of(application));

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.APPLIED); // invalid

        assertThrows(CustomUnauthorizedEntityActionException.class,
                () -> applicationService.updateStatus(APPLICATION_ID, request));
    }

    @Test void shouldThrowIfIllegalDomainTransition(){
        application.reject(); // already rejected

        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(orgAdminUser);
        when(applicationRepo.findByIdAndOrganisationId(APPLICATION_ID, orgAdminUser.getOrganisationId()))
                .thenReturn(Optional.of(application));

        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.REJECTED);

        assertThrows(CustomUnauthorizedEntityActionException.class,
                () -> applicationService.updateStatus(APPLICATION_ID, request));
    }

    @Test void shouldReturnApplicationsForCandidate(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);

        Page<Application> page = new PageImpl<>(List.of(application));

        when(applicationRepo.findByCandidateId(eq(candidateUser.getId()), any())).thenReturn(page);
        when(applicationMapper.toResponse(application))
                .thenReturn(new ApplicationResponseDto(APPLICATION_ID, JOB_ID, ApplicationStatus.APPLIED));

        Page<ApplicationResponseDto> result = applicationService.getApplications(0, "createdAt", "desc");

        assertEquals(1, result.getContent().size());

        verify(applicationRepo).findByCandidateId(eq(candidateUser.getId()), any());
    }

    @Test void shouldReturnApplicationsForRecruiter(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);

        List<Long> jobIds = List.of(JOB_ID, JOB_ID + 1);

        when(jobRepo.findJobIdsByAssignedRecruiterId(recruiterUser.getId())).thenReturn(jobIds);

        Page<Application> page = new PageImpl<>(List.of(application));

        when(applicationRepo.findByJobIdIn(eq(jobIds), any())).thenReturn(page);
        when(applicationMapper.toResponse(application))
                .thenReturn(new ApplicationResponseDto(APPLICATION_ID, JOB_ID, ApplicationStatus.APPLIED));

        Page<ApplicationResponseDto> result = applicationService.getApplications(0, "createdAt", "asc");

        assertEquals(1, result.getContent().size());

        verify(jobRepo).findJobIdsByAssignedRecruiterId(recruiterUser.getId());
        verify(applicationRepo).findByJobIdIn(eq(jobIds), any());
    }

    @Test void shouldReturnApplicationsForOrgAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);

        Page<Application> page = new PageImpl<>(List.of(application));

        when(applicationRepo.findByOrganisationId(eq(orgAdminUser.getOrganisationId()), any())).thenReturn(page);
        when(applicationMapper.toResponse(application))
                .thenReturn(new ApplicationResponseDto(APPLICATION_ID, JOB_ID, ApplicationStatus.APPLIED));

        Page<ApplicationResponseDto> result = applicationService.getApplications(0, "createdAt", "desc");

        assertEquals(1, result.getContent().size());

        verify(applicationRepo).findByOrganisationId(eq(orgAdminUser.getOrganisationId()), any());
    }

    @Test void shouldThrowIfUserUnauthorized(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(supportUser);

        assertThrows(CustomUnauthorizedException.class,
                () -> applicationService.getApplications(0, "createdAt", "desc"));
    }

    @Test void shouldPassCorrectPageable(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        when(applicationRepo.findByCandidateId(eq(candidateUser.getId()), any())).thenReturn(Page.empty());

        applicationService.getApplications(1, "createdAt", "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(applicationRepo).findByCandidateId(eq(candidateUser.getId()), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertNotNull(order, "Sort order for 'createdAt' should not be null");
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

}
