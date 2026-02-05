package com.avinashee0012.hireflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.service.JobService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@AllArgsConstructor
public class JobController {

    private final JobService jobService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN')")
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody JobRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN','CANDIDATE','SUPPORT')")
    public ResponseEntity<JobResponseDto> getJob(@PathVariable Long jobId){
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobById(jobId));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN')")
    public ResponseEntity<JobResponseDto> updateJob(@PathVariable Long jobId, @Valid @RequestBody JobRequestDto request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobService.updateJob(jobId, request));
    }

    @PatchMapping("/{jobId}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> closeJob(@PathVariable Long jobId){
        jobService.closeJob(jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{jobId}/reopen")
    @PreAuthorize("hasRole('ORGADMIN')")
    public ResponseEntity<Void> reopenJob(@PathVariable Long jobId){
        jobService.reopenJob(jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
