package com.avinashee0012.hireflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.avinashee0012.hireflow.config.jwt.JwtService;
import com.avinashee0012.hireflow.config.security.CustomUserDetailsService;
import com.avinashee0012.hireflow.domain.enums.JobStatus;
import com.avinashee0012.hireflow.dto.request.JobRequestDto;
import com.avinashee0012.hireflow.dto.response.JobResponseDto;
import com.avinashee0012.hireflow.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(JobController.class)
@Import(TestSecurityConfig.class)
public class JobControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldCreateJob() throws Exception{
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Backend Developer");
        request.setDescription("Spring Boot backend role");

        JobResponseDto response = new JobResponseDto(1L, "Backend Developer", "Spring Boot backend role",
                JobStatus.OPEN, 5L);

        when(jobService.createJob(any())).thenReturn(response);

        mockMvc.perform(post("/api/jobs").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(jobService).createJob(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldReturnUnauthorizedWhenCandidateCreatesJob() throws Exception{
        mockMvc.perform(post("/api/jobs").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(jobService, never()).createJob(any());
    }

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldReturnBadRequestWhenTitleInvalid() throws Exception{
        JobRequestDto request = new JobRequestDto();
        request.setTitle("abc"); // too short
        request.setDescription("Valid description");

        mockMvc.perform(post("/api/jobs").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

        verify(jobService, never()).createJob(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldGetJob() throws Exception{
        JobResponseDto response = new JobResponseDto(1L, "Backend Developer", "Spring Boot backend role",
                JobStatus.OPEN, 5L);

        when(jobService.getJobById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/jobs/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(jobService).getJobById(1L);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldUpdateJob() throws Exception{
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");

        JobResponseDto response = new JobResponseDto(1L, "Updated Title", "Updated description", JobStatus.OPEN, 5L);

        when(jobService.updateJob(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/jobs/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(jobService).updateJob(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldCloseJob() throws Exception{
        mockMvc.perform(patch("/api/jobs/1/close").with(csrf())).andExpect(status().isAccepted());

        verify(jobService).closeJob(1L);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldReturnUnauthorizedWhenOrgAdminClosesJob() throws Exception{
        mockMvc.perform(patch("/api/jobs/1/close").with(csrf())).andExpect(status().isUnauthorized());

        verify(jobService, never()).closeJob(any());
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldReopenJob() throws Exception{
        mockMvc.perform(patch("/api/jobs/1/reopen").with(csrf())).andExpect(status().isAccepted());

        verify(jobService).reopenJob(1L);
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldGetJobs() throws Exception{
        Page<JobResponseDto> page = new PageImpl<>(List.of(new JobResponseDto(1L, "Backend Developer", "Spring Boot backend role", JobStatus.OPEN, 5L)));

        when(jobService.getJobs(0, "createdAt", "desc")).thenReturn(page);

        mockMvc.perform(get("/api/jobs").param("page", "0").param("sortby", "createdAt").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1L));

        verify(jobService).getJobs(0, "createdAt", "desc");
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldReturnBadRequestForInvalidSortField() throws Exception{
        mockMvc.perform(get("/api/jobs").param("sortby", "invalid")).andExpect(status().isBadRequest());
    }

    @Test void shouldReturnForbiddenWhenNotAuthenticated() throws Exception{
        mockMvc.perform(get("/api/jobs/1")).andExpect(status().isForbidden());
    }
}
