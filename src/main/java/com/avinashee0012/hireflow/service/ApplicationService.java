package com.avinashee0012.hireflow.service;

import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;

public interface ApplicationService {
    ApplicationResponseDto apply(Long jobId);
    void withdraw(Long applicationId);
    ApplicationResponseDto updateStatus(Long applicationId, ApplicationStatusUpdateRequestDto request);
}
