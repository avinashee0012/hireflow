package com.avinashee0012.hireflow.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avinashee0012.hireflow.domain.entity.Application;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long>{
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    Optional<Application> findByIdAndCandidateId(Long applicationId, Long candidateId);

    Optional<Application> findByIdAndOrganisationId(Long applicationId, Long organisationId);

    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    Page<Application> findByOrganisationId(Long organisationId, Pageable pageable);

    Page<Application> findByJobIdIn(Collection<Long> jobIds, Pageable pageable);
}
