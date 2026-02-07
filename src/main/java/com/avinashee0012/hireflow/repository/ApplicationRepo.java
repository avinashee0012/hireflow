package com.avinashee0012.hireflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avinashee0012.hireflow.domain.entity.Application;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long>{
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Optional<Application> findByIdAndCandidateId(Long applicationId, Long candidateId);
    Optional<Application> findByIdAndJobAssignedRecruiterId(Long applicationId, Long recruiterId);
    Optional<Application> findByIdAndOrganisationId(Long applicationId, Long organisationId);
}

