package com.avinashee0012.hireflow.mapper;

import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.Job;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;

@Component
public class JobMapper {
    
    public JobResponseDto toResponse(Job job){
        return new JobResponseDto(job.getId(), job.getTitle(), job.getDescription(), job.getJobStatus(), job.getAssignedRecruiterId());
    }
}
