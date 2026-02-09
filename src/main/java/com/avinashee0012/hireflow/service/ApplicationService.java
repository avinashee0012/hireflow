package com.avinashee0012.hireflow.service;

import org.springframework.data.domain.Page;

import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;

public interface ApplicationService{
    ApplicationResponseDto apply(Long jobId);

    void withdraw(Long applicationId);

    ApplicationResponseDto updateStatus(Long applicationId, ApplicationStatusUpdateRequestDto request);

    Page<ApplicationResponseDto> getApplications(int page, String sortby, String direction);
}
