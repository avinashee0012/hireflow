package com.avinashee0012.hireflow.service.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.User;
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
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;
    private final CurrentUserProvider currentUserProvider;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponseDto createJob(JobRequestDto request) {
        User user = currentUserProvider.getAuthenticatedUser();
        boolean allowed = user.hasRole("RECRUITER") || user.hasRole("ORGADMIN");
        if (!allowed)
            throw new CustomUnauthorizedException("User cannot create jobs");
        Job job = new Job(request.getTitle(), request.getDescription(), user.getOrganisationId(), user.getId());
        return jobMapper.toResponse(jobRepo.save(job));
    }

    @Override
    @Transactional
    public void closeJob(Long jobId) {
        User user = currentUserProvider.getAuthenticatedUser();
        Job job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId())
                .orElseThrow(() -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        job.close();
    }

    @Override
    @Transactional
    public void reopenJob(Long jobId) {
        User user = currentUserProvider.getAuthenticatedUser();
        if (!user.hasRole("ORGADMIN"))
            throw new CustomUnauthorizedException("Only ORGADMIN can reopen jobs");
        Job job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        job.reopen();
    }

    @Override
    public JobResponseDto getJobById(Long jobId) {
        User user = currentUserProvider.getAuthenticatedUser();
        Job job;
        if (user.hasRole("ORGADMIN")) {
            job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        } else if (user.hasRole("RECRUITER")) {
            job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId()).orElseThrow(
                    () -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        } else {
            throw new CustomUnauthorizedException("Access denied");
        }
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional
    public JobResponseDto updateJob(Long jobId, JobRequestDto request) {
        User user = currentUserProvider.getAuthenticatedUser();
        Job job;
        if (user.hasRole("ORGADMIN")) {
            job = jobRepo.findByIdAndOrganisationId(jobId, user.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Job not found in organisation"));
        } else if (user.hasRole("RECRUITER")) {
            job = jobRepo.findByIdAndAssignedRecruiterId(jobId, user.getId()).orElseThrow(
                    () -> new CustomUnauthorizedEntityActionException("Job not assigned to this recruiter"));
        } else {
            throw new CustomUnauthorizedException("Access denied");
        }
        job.updateDetails(request.getTitle(), request.getDescription());
        return jobMapper.toResponse(job);
    }
}
