package com.avinashee0012.hireflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;
import com.avinashee0012.hireflow.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/applications")
@AllArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponseDto> applyToJob(@PathVariable Long jobId){
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(jobId));
    }
    
    @PatchMapping("/{applicationId}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId){
        applicationService.withdraw(applicationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasAnyRole('RECRUITER','ORGADMIN')")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(@PathVariable Long applicationId, @Valid @RequestBody ApplicationStatusUpdateRequestDto request){
        return ResponseEntity.status(HttpStatus.OK).body(applicationService.updateStatus(applicationId, request));
    }
}
