package com.avinashee0012.hireflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.config.security.OrganisationAccessGuard;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.domain.enums.JobStatus;
import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.JobMapper;
import com.avinashee0012.hireflow.repository.JobRepo;
import com.avinashee0012.hireflow.service.JobService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class JobServiceImpl implements JobService{
    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
    private static int PAGE_SIZE = 10;

    private final JobRepo jobRepo;
    private final CurrentUserProvider currentUserProvider;
    private final JobMapper jobMapper;
    private final OrganisationAccessGuard organisationAccessGuard;

    @Override
    @Transactional public JobResponseDto createJob(JobRequestDto request){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        boolean allowed = user.hasRole("RECRUITER") || user.hasRole("ORGADMIN");
        if (!allowed)
            throw new CustomUnauthorizedException("User cannot create jobs");
        Job job = new Job(request.getTitle(), request.getDescription(), user.getOrganisationId(), user.getId());
        Job savedJob = jobRepo.save(job);
        log.info("Job created: jobId={}, orgId={}, recruiterId={}", savedJob.getId(), savedJob.getOrganisationId(),
                user.getId());
        return jobMapper.toResponse(savedJob);
    }

    @Override
    @Transactional public void closeJob(Long jobId){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        Job job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId())
                .orElseThrow(() -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        job.close();
        log.info("Job closed: jobId={}, recruiterId={}", jobId, user.getId());
    }

    @Override
    @Transactional public void reopenJob(Long jobId){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        if (!user.hasRole("ORGADMIN"))
            throw new CustomUnauthorizedException("Only ORGADMIN can reopen jobs");
        Job job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        job.reopen();
        log.info("Job reopened: jobId={}, orgAdminId={}", jobId, user.getId());
    }

    @Override public JobResponseDto getJobById(Long jobId){
        User user = currentUserProvider.getAuthenticatedUser();
        Job job;
        if (user.hasRole("ORGADMIN")){
            job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        } else if (user.hasRole("RECRUITER")){
            job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId()).orElseThrow(
                    () -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        } else{
            throw new CustomUnauthorizedException("Access denied");
        }
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional public JobResponseDto updateJob(Long jobId, JobRequestDto request){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        Job job;
        if (user.hasRole("ORGADMIN")){
            job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        } else if (user.hasRole("RECRUITER")){
            job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId()).orElseThrow(
                    () -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        } else{
            throw new CustomUnauthorizedException("Access denied");
        }
        job.updateDetails(request.getTitle(), request.getDescription());
        log.info("Job updated: jobId={}, updatedByUserId={}", jobId, user.getId());
        return jobMapper.toResponse(job);
    }

    @Override public Page<JobResponseDto> getJobs(int page, String sortBy, String direction){
        User user = currentUserProvider.getAuthenticatedUser();
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sort);
        Page<Job> jobs;
        if (user.hasRole("RECRUITER")){
            jobs = jobRepo.findByAssignedRecruiterId(user.getId(), pageable);
        } else if (user.hasRole("ORGADMIN")){
            jobs = jobRepo.findByOrganisationId(user.getOrganisationId(), pageable);
        } else if (user.hasRole("CANDIDATE")){
            jobs = jobRepo.findByJobStatus(JobStatus.OPEN, pageable);
        } else if (user.hasRole("SUPPORT")){
            jobs = jobRepo.findByOrganisationId(user.getOrganisationId(), pageable);
        } else{
            throw new CustomUnauthorizedException("Access denied");
        }
        return jobs.map(jobMapper::toResponse);
    }

}
