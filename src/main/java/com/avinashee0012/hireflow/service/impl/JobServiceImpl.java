package com.avinashee0012.hireflow.service.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.JobMapper;
import com.avinashee0012.hireflow.repository.JobRepo;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.service.JobService;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepo jobRepo;
    private final RoleRepo roleRepo;
    private final CurrentUserProvider currentUserProvider;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponseDto createJob(JobRequestDto request) {
        User user = currentUserProvider.getAuthenticatedUser();
        Role role = roleRepo.findByName("RECRUITER").orElseThrow(() -> new IllegalStateException());
        if(!user.getRoles().contains(role)) throw new CustomUnauthorizedException();
        Long organisationId = user.getOrganisationId();
        Long assignedRecruiterId = user.getId();
        Job job = new Job(request.getTitle(), request.getDescription(), organisationId, assignedRecruiterId);
        Job savedJob = jobRepo.save(job);
        return jobMapper.toResponse(savedJob);
    }

}
