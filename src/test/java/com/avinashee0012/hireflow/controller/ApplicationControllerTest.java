package com.avinashee0012.hireflow.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
import com.avinashee0012.hireflow.domain.enums.ApplicationStatus;
import com.avinashee0012.hireflow.dto.request.ApplicationStatusUpdateRequestDto;
import com.avinashee0012.hireflow.dto.response.ApplicationResponseDto;
import com.avinashee0012.hireflow.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ApplicationController.class)
@Import(TestSecurityConfig.class)
public class ApplicationControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private ApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldApplyToJob() throws Exception{
        ApplicationResponseDto response = new ApplicationResponseDto(1L, 10L, ApplicationStatus.APPLIED);

        when(applicationService.apply(10L)).thenReturn(response);

        mockMvc.perform(post("/api/applications/10").with(csrf())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(1L)).andExpect(jsonPath("$.jobId").value(10L))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(applicationService).apply(10L);
    }

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldReturnUnauthorizedWhenRecruiterApplies() throws Exception{
        mockMvc.perform(post("/api/applications/10").with(csrf())).andExpect(status().isUnauthorized());

        verify(applicationService, never()).apply(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldWithdrawApplication() throws Exception{
        mockMvc.perform(patch("/api/applications/1/withdraw").with(csrf())).andExpect(status().isNoContent());

        verify(applicationService).withdraw(1L);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldUpdateApplicationStatus() throws Exception{
        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        ApplicationResponseDto response = new ApplicationResponseDto(1L, 10L, ApplicationStatus.SHORTLISTED);

        when(applicationService.updateStatus(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/applications/1/status").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        verify(applicationService).updateStatus(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldReturnUnauthorizedWhenCandidateUpdatesStatus() throws Exception{
        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        mockMvc.perform(patch("/api/applications/1/status").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized());

        verify(applicationService, never()).updateStatus(any(), any());
    }

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldReturnBadRequestWhenStatusNull() throws Exception{
        mockMvc.perform(
                patch("/api/applications/1/status").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).updateStatus(any(), any());
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldGetApplications() throws Exception{
        Page<ApplicationResponseDto> page = new PageImpl<>(
                List.of(new ApplicationResponseDto(1L, 10L, ApplicationStatus.APPLIED)));

        when(applicationService.getApplications(0, "createdAt", "desc")).thenReturn(page);

        mockMvc.perform(
                get("/api/applications").param("page", "0").param("sortby", "createdAt").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].applicationId").value(1L));

        verify(applicationService).getApplications(0, "createdAt", "desc");
    }

    @Test void shouldReturnForbiddenWhenNotAuthenticated() throws Exception{
        mockMvc.perform(get("/api/applications")).andExpect(status().isForbidden());
    }
}
