package com.avinashee0012.hireflow.mapper;

import org.springframework.stereotype.Component;

import com.avinashee0012.hireflow.domain.entity.Application;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;

@Component
public class ApplicationMapper{
    public ApplicationResponseDto toResponse(Application application){
        return new ApplicationResponseDto(application.getId(), application.getJobId(),
                application.getApplicationStatus());
    }
}
