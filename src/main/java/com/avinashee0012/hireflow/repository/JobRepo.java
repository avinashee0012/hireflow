package com.avinashee0012.hireflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.domain.enums.JobStatus;

@Repository
public interface JobRepo extends JpaRepository<Job, Long>{
    Optional<Job> findByIdAndOrganisationId(Long jobId, Long organisationId);

    boolean existsByIdAndAssignedRecruiterId(Long jobId, Long recruiterId);

    Optional<Job> findByIdAndAssignedRecruiterId(Long jobId, Long recruiterId);

    Page<Job> findByAssignedRecruiterId(Long recruiterId, Pageable pageable);

    Page<Job> findByOrganisationId(Long organisationId, Pageable pageable);

    Page<Job> findByJobStatus(JobStatus status, Pageable pageable);

    List<Long> findJobIdsByAssignedRecruiterId(Long recruiterId);
}
