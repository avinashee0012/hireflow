package com.avinashee0012.hireflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avinashee0012.hireflow.domain.entity.Job;

@Repository
public interface JobRepo extends JpaRepository<Job, Long> {
    Optional<Job> findByIdAndOrganisationId(Long jobId, Long organisationId);
    boolean existsByIdAndAssignedRecruiterId(Long jobId, Long recruiterId);
    Optional<Job> findByIdAndAssignedRecruiterId(Long jobId, Long recruiterId);
}
