package com.avinashee0012.hireflow.service;

import com.avinashee0012.hireflow.dto.request.OrganisationRequestDto;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;

public interface OrganisationService {

    OrganisationResponseDto createOrganisation(OrganisationRequestDto request);

    void suspendOrganisation(Long orgId);

    void activateOrganisation(Long orgId);

    OrganisationResponseDto getOrganisation(Long orgId);
    
}
