package com.avinashee0012.hireflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.avinashee0012.hireflow.config.security.CurrentUserProvider;
import com.avinashee0012.hireflow.config.security.OrganisationAccessGuard;
import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.domain.enums.JobStatus;
import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;
import com.avinashee0012.hireflow.exception.CustomDuplicateEntityException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedException;
import com.avinashee0012.hireflow.mapper.ApplicationMapper;
import com.avinashee0012.hireflow.repository.ApplicationRepo;
import com.avinashee0012.hireflow.repository.JobRepo;
import com.avinashee0012.hireflow.service.ApplicationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService{
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static int PAGE_SIZE = 10;

    private final ApplicationRepo applicationRepo;
    private final JobRepo jobRepo;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationMapper applicationMapper;
    private final OrganisationAccessGuard organisationAccessGuard;

    @Override
    @Transactional public ApplicationResponseDto apply(Long jobId){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        boolean allowed = user.hasRole("CANDIDATE");
        if (!allowed)
            throw new CustomUnauthorizedException("Only CANDIDATE can apply to jobs");
        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found with id: " + jobId));
        if (job.getJobStatus() != JobStatus.OPEN)
            throw new IllegalStateException("Can only apply to OPEN jobs");
        boolean alreadyApplied = applicationRepo.existsByJobIdAndCandidateId(jobId, user.getId());
        if (alreadyApplied)
            throw new CustomDuplicateEntityException("Already applied to this job");
        Application application = new Application(jobId, user.getId(), job.getOrganisationId());
        Application savedApplication = applicationRepo.save(application);
        log.info("Application submitted: applicationId={}, jobId={}, candidateId={}", savedApplication.getId(), jobId,
                user.getId());
        return new ApplicationResponseDto(savedApplication.getId(), jobId, savedApplication.getApplicationStatus());
    }

    @Override
    @Transactional public void withdraw(Long applicationId){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        boolean allowed = user.hasRole("CANDIDATE");
        if (!allowed)
            throw new CustomUnauthorizedException("Only CANDIDATE can withdraw their application");
        Application application = applicationRepo.findByIdAndCandidateId(applicationId, user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Application not found with id " + applicationId + " for this user"));
        application.withdraw();
        log.info("Application withdrawn: applicationId={}, candidateId={}", applicationId, user.getId());
    }

    @Override
    @Transactional public ApplicationResponseDto updateStatus(Long applicationId,
            ApplicationStatusUpdateRequestDto request){
        User user = currentUserProvider.getAuthenticatedUser();
        organisationAccessGuard.ensureActiveOrganisation(user);
        boolean allowed = user.hasRole("RECRUITER") || user.hasRole("ORGADMIN");
        if (!allowed)
            throw new CustomUnauthorizedException("Only RECRUITER or ORGADMIN can update status");
        Application application;
        if (user.hasRole("ORGADMIN")){ // ORGADMIN can update any application
            application = applicationRepo.findByIdAndOrganisationId(applicationId, user.getOrganisationId())
                    .orElseThrow(() -> new CustomUnauthorizedEntityActionException(
                            "Application does not belong to organisation"));
        } else{ // RECRUITER can only update applications for their own job
            application = applicationRepo.findById(applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application not found with id: " + applicationId));
            if (!jobRepo.existsByIdAndAssignedRecruiterId(application.getJobId(), user.getId())){
                throw new CustomUnauthorizedEntityActionException("Application not associated with recruiter");
            }
        }
        switch (request.getStatus()) {
            case SHORTLISTED:
                application.shortlist();
                break;
            case REJECTED:
                application.reject();
                break;
            default:
                throw new CustomUnauthorizedEntityActionException("Invalid transition status");
        }
        log.info("Application status updated: applicationId={}, newStatus={}, updatedByUserId={}", applicationId,
                request.getStatus(), user.getId());
        return applicationMapper.toResponse(application);
    }

    @Override public Page<ApplicationResponseDto> getApplications(int page, String sortBy, String direction){
        User user = currentUserProvider.getAuthenticatedUser();
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sort);
        Page<Application> applications;
        if (user.hasRole("CANDIDATE")){
            applications = applicationRepo.findByCandidateId(user.getId(), pageable);
        } else if (user.hasRole("RECRUITER")){
            applications = applicationRepo.findByJobIdIn(jobRepo.findJobIdsByAssignedRecruiterId(user.getId()),
                    pageable);
        } else if (user.hasRole("ORGADMIN")){
            applications = applicationRepo.findByOrganisationId(user.getOrganisationId(), pageable);
        } else{
            throw new CustomUnauthorizedException("Access denied");
        }
        return applications.map(applicationMapper::toResponse);
    }

}
