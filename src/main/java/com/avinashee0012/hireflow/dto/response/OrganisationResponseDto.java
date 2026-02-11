package com.avinashee0012.hireflow.dto.response;

import com.avinashee0012.hireflow.domain.enums.OrganisationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganisationResponseDto {
    private Long id;
    private String name;
    private OrganisationStatus status;
    private Long adminUserId;
}
