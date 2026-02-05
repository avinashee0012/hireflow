package com.avinashee0012.hireflow.dto.response;

import com.avinashee0012.hireflow.domain.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobResponseDto {
    private Long id;
    private String title;
    private String description;
    private JobStatus status;
    private Long assignedRecruiterId;
}
