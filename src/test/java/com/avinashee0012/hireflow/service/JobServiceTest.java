package com.avinashee0012.hireflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.config.security.OrganisationAccessGuard;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.domain.enums.JobStatus;
import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.JobMapper;
import com.avinashee0012.hireflow.repository.JobRepo;
import com.avinashee0012.hireflow.service.impl.JobServiceImpl;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest{

    @Mock
    private JobRepo jobRepo;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private OrganisationAccessGuard organisationAccessGuard;

    @InjectMocks
    private JobServiceImpl jobService;

    private User recruiterUser;
    private User orgAdminUser;
    private User candidateUser;
    private Role recruiterRole;
    private Role orgAdminRole;
    private Role candidateRole;
    private JobRequestDto request;
    private Job job;

    private static final Long ORG_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long JOB_ID = 100L;

    @BeforeEach void setup(){
        recruiterRole = new Role("RECRUITER");
        orgAdminRole = new Role("ORGADMIN");
        candidateRole = new Role("CANDIDATE");

        recruiterUser = new User("recruiter@email.com", "encryptedPass", ORG_ID);
        recruiterUser.assignRole(recruiterRole);
        ReflectionTestUtils.setField(recruiterUser, "id", USER_ID);

        orgAdminUser = new User("orgadmin@email.com", "encryptedPass", ORG_ID);
        orgAdminUser.assignRole(orgAdminRole);
        ReflectionTestUtils.setField(orgAdminUser, "id", USER_ID);

        candidateUser = new User("candidate@email.com", "encryptedPass", null);
        candidateUser.assignRole(candidateRole);
        ReflectionTestUtils.setField(candidateUser, "id", USER_ID);

        request = new JobRequestDto();
        request.setTitle("Backend Dev");
        request.setDescription("Spring Boot");

        job = new Job(request.getTitle(), request.getDescription(), recruiterUser.getOrganisationId(),
                recruiterUser.getId());
        ReflectionTestUtils.setField(job, "id", JOB_ID);
    }

    @Test void shouldCreateJobSuccessfullyForRecruiter(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);

        when(jobRepo.save(any(Job.class))).thenReturn(job);
        when(jobMapper.toResponse(any(Job.class))).thenReturn(new JobResponseDto(JOB_ID, "Backend Dev", "Spring Boot",
                job.getJobStatus(), job.getAssignedRecruiterId()));

        JobResponseDto response = jobService.createJob(request);

        assertNotNull(response);
        assertEquals(JOB_ID, response.getId());

        verify(jobRepo).save(any());
    }

    @Test void shouldThrowIfUserCannotCreateJob(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(candidateUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(candidateUser);

        assertThrows(CustomUnauthorizedException.class, () -> jobService.createJob(request));

        verify(jobRepo, never()).save(any());
    }

    @Test void shouldCloseJobSuccessfully(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);
        when(jobRepo.findByIdAndAssignedRecruiterId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));

        jobService.closeJob(JOB_ID);

        assertEquals(JobStatus.CLOSED, job.getJobStatus());
    }

    @Test void shouldReopenJobForOrgAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(orgAdminUser);

        job.close();

        when(jobRepo.findByIdAndOrganisationId(JOB_ID, ORG_ID)).thenReturn(Optional.of(job));

        jobService.reopenJob(JOB_ID);

        assertEquals(JobStatus.OPEN, job.getJobStatus());
    }

    @Test void shouldReturnJobForOrgAdmin(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(orgAdminUser);
        when(jobRepo.findByIdAndOrganisationId(JOB_ID, ORG_ID)).thenReturn(Optional.of(job));
        when(jobMapper.toResponse(job)).thenReturn(new JobResponseDto(JOB_ID, "Backend Dev", "Spring Boot",
                job.getJobStatus(), job.getAssignedRecruiterId()));

        JobResponseDto response = jobService.getJobById(JOB_ID);

        assertNotNull(response);
    }

    @Test void shouldUpdateJobForRecruiter(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        doNothing().when(organisationAccessGuard).ensureActiveOrganisation(recruiterUser);
        when(jobRepo.findByIdAndAssignedRecruiterId(JOB_ID, USER_ID)).thenReturn(Optional.of(job));
        when(jobMapper.toResponse(job)).thenReturn(new JobResponseDto(JOB_ID, "Backend Dev", "Spring Boot",
                job.getJobStatus(), job.getAssignedRecruiterId()));

        JobResponseDto response = jobService.updateJob(JOB_ID, request);

        assertEquals("Backend Dev", response.getTitle());
        assertEquals("Spring Boot", response.getDescription());
    }

    @Test void shouldReturnJobsForRecruiter(){
        when(currentUserProvider.getAuthenticatedUser()).thenReturn(recruiterUser);
        Page<Job> page = new PageImpl<>(List.of(job));
        when(jobRepo.findByAssignedRecruiterId(eq(USER_ID), any())).thenReturn(page);
        when(jobMapper.toResponse(any())).thenReturn(new JobResponseDto(JOB_ID, "Backend Dev", "Spring Boot",
                job.getJobStatus(), job.getAssignedRecruiterId()));

        Page<JobResponseDto> result = jobService.getJobs(0, "createdAt", "desc");

        assertEquals(1, result.getContent().size());
    }
}
