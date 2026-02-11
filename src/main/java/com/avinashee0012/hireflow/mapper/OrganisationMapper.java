package com.avinashee0012.hireflow.mapper;

import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.Organisation;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;

@Component
public class OrganisationMapper {
    public OrganisationResponseDto toResponse(Organisation organisation){
        return new OrganisationResponseDto(organisation.getId(), organisation.getName(), organisation.getStatus(), organisation.getOrgAdminUserId());
    }
}
