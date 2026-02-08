package com.avinashee0012.hireflow.service;

import org.springframework.data.domain.Page;

import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;

public interface JobService{
    JobResponseDto createJob(JobRequestDto request);

    JobResponseDto getJobById(Long jobId);

    JobResponseDto updateJob(Long jobId, JobRequestDto request);

    void closeJob(Long jobId);

    void reopenJob(Long jobId);

    Page<JobResponseDto> getJobs(int page, String sortby, String direction);
}
