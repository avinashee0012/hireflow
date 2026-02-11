package com.avinashee0012.hireflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avinashee0012.hireflow.dto.request.OrganisationRequestDto;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;
import com.avinashee0012.hireflow.service.OrganisationService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/organisations")
@AllArgsConstructor
public class OrganisationController{

    private final OrganisationService organisationService;

    @PostMapping
    @PreAuthorize("hasRole('SUPPORT')") 
    public ResponseEntity<OrganisationResponseDto> createOrganisation(
            @Valid @RequestBody OrganisationRequestDto request){
        return ResponseEntity.status(HttpStatus.CREATED).body(organisationService.createOrganisation(request));
    }

    @PatchMapping("/{orgId}/suspend")
    @PreAuthorize("hasRole('SUPPORT')") 
    public ResponseEntity<Void> suspendOrganisation(@PathVariable Long orgId){
        organisationService.suspendOrganisation(orgId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{orgId}/activate")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<Void> activateOrganisation(@PathVariable Long orgId){
        organisationService.activateOrganisation(orgId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{orgId}")
    @PreAuthorize("hasAnyRole('SUPPORT', 'ORGADMIN')") 
    public ResponseEntity<OrganisationResponseDto> getOrganisation(
            @PathVariable Long orgId){
        return ResponseEntity.status(HttpStatus.OK).body(organisationService.getOrganisation(orgId));
    }
}
