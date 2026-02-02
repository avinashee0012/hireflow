package com.avinashee0012.hireflow.domain.entity;

import com.avinashee0012.hireflow.domain.enums.JobStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jobs")
@Getter
@NoArgsConstructor
public class Job extends Auditor{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(name = "organisation_id", nullable = false)
    private Long organisationId;

    @Column(name = "assigned_recruiter_id", nullable = false)
    private Long assignedRecruiterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus jobStatus = JobStatus.OPEN;

    public Job(String title, String description, Long organisationId, Long assignedRecruiterId) {
        this.title = title;
        this.description = description;
        this.organisationId = organisationId;
        this.assignedRecruiterId = assignedRecruiterId;
    }

    public void close(){
        if (jobStatus == JobStatus.CLOSED) {
            throw new IllegalStateException("Job is already closed");
        }
        this.jobStatus = JobStatus.CLOSED;
    }

    public void reopen(){
        if (jobStatus == JobStatus.OPEN) {
            throw new IllegalStateException("Job is already closed");
        }
        this.jobStatus = JobStatus.OPEN;
    }

    public void reassignRecruiter(Long recruiterId){
        if (recruiterId == null) {
            throw new IllegalArgumentException("Recruiter id cannot be null");
        }
        this.assignedRecruiterId = recruiterId;
    }
}
