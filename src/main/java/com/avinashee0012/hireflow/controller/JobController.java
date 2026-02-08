package com.avinashee0012.hireflow.controller;

import java.util.Set;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.service.JobService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@AllArgsConstructor
public class JobController{

    private static Set<String> ALLOWED_SORT_FIELDS = Set.of("createdat", "updateat", "status");
    private static Set<String> ALLOWED_DIRECTION_FIELDS = Set.of("asc", "desc");

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN')") public ResponseEntity<JobResponseDto> createJob(
            @Valid @RequestBody JobRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN','CANDIDATE','SUPPORT')") public ResponseEntity<JobResponseDto> getJob(
            @PathVariable Long jobId){
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobById(jobId));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN')") public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long jobId, @Valid @RequestBody JobRequestDto request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobService.updateJob(jobId, request));
    }

    @PatchMapping("/{jobId}/close")
    @PreAuthorize("hasRole('RECRUITER')") public ResponseEntity<Void> closeJob(@PathVariable Long jobId){
        jobService.closeJob(jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{jobId}/reopen")
    @PreAuthorize("hasRole('ORGADMIN')") public ResponseEntity<Void> reopenJob(@PathVariable Long jobId){
        jobService.reopenJob(jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN','CANDIDATE','SUPPORT')") public ResponseEntity<Page<JobResponseDto>> getJobs(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "createdAt") String sortby,
            @RequestParam(defaultValue = "desc") String direction){
        if (!ALLOWED_SORT_FIELDS.contains(sortby.toLowerCase()))
            throw new IllegalArgumentException("Invalid sort field");
        if (!ALLOWED_DIRECTION_FIELDS.contains(direction.toLowerCase()))
            throw new IllegalArgumentException("Invalid sort direction field");
        return ResponseEntity.status(HttpStatus.OK).body(jobService.getJobs(page, sortby, direction));
    }
}
