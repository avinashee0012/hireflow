package com.avinashee0012.hireflow.domain.entity;

import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;
import com.avinashee0012.hireflow.exception.CustomUnauthorizedEntityActionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
})
@Getter
@NoArgsConstructor
public class Application extends Auditor{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private Long organisationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus applicationStatus;

    public Application(Long jobId, Long candidateId, Long organisationId) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.organisationId = organisationId;
        this.applicationStatus = ApplicationStatus.APPLIED;
    }

    public void withdraw(){
        if(applicationStatus != ApplicationStatus.APPLIED)
            throw new CustomUnauthorizedEntityActionException("Invalid transition");
        this.applicationStatus = ApplicationStatus.WITHDRAWN;
    }

    public void shortlist(){
        if(applicationStatus != ApplicationStatus.APPLIED)
            throw new CustomUnauthorizedEntityActionException("Invalid transition");
        this.applicationStatus = ApplicationStatus.SHORTLISTED;
    }

    public void reject(){
        if(applicationStatus != ApplicationStatus.APPLIED)
            throw new CustomUnauthorizedEntityActionException("Invalid lifecycle transition");
        this.applicationStatus = ApplicationStatus.REJECTED;
    }
}
