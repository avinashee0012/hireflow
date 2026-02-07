package com.avinashee0012.hireflow.dto.response;

import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationResponseDto {
    private Long applicationId;
    private Long jobId;
    private ApplicationStatus status;
}
