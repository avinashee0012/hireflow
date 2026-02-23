package com.avinashee0012.hireflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.avinashee0012.hireflow.config.jwt.JwtService;
import com.avinashee0012.hireflow.config.security.CustomUserDetailsService;
import com.avinashee0012.hireflow.domain.enums.OrganisationStatus;
import com.avinashee0012.hireflow.dto.request.OrganisationRequestDto;
import com.avinashee0012.hireflow.dto.response.OrganisationResponseDto;
import com.avinashee0012.hireflow.service.OrganisationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrganisationController.class)
@Import(TestSecurityConfig.class)
class OrganisationControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private OrganisationService organisationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldCreateOrganisation() throws Exception{
        OrganisationRequestDto request = new OrganisationRequestDto();
        request.setName("TestOrg");
        request.setAdminUserId(2L);

        OrganisationResponseDto response = new OrganisationResponseDto(1L, "TestOrg", OrganisationStatus.ACTIVE, 2L);

        when(organisationService.createOrganisation(any(OrganisationRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/organisations").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(organisationService).createOrganisation(any());
    }

    @Test
    @WithMockUser(roles = "RECRUITER") void shouldReturnUnauthorizedWhenNotSupport() throws Exception{
        OrganisationRequestDto request = new OrganisationRequestDto();
        request.setName("TestOrg");
        request.setAdminUserId(2L);

        mockMvc.perform(post("/api/organisations").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized());

        verify(organisationService, never()).createOrganisation(any());
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldSuspendOrganisation() throws Exception{
        mockMvc.perform(patch("/api/organisations/1/suspend").with(csrf())).andExpect(status().isNoContent());

        verify(organisationService).suspendOrganisation(1L);
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldActivateOrganisation() throws Exception{
        mockMvc.perform(patch("/api/organisations/1/activate").with(csrf())).andExpect(status().isNoContent());

        verify(organisationService).activateOrganisation(1L);
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldGetOrganisationAsSupport() throws Exception{
        OrganisationResponseDto response = new OrganisationResponseDto(1L, "TestOrg", OrganisationStatus.ACTIVE, 2L);

        when(organisationService.getOrganisation(1L)).thenReturn(response);

        mockMvc.perform(get("/api/organisations/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestOrg"));

        verify(organisationService).getOrganisation(1L);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldGetOrganisationAsOrgAdmin() throws Exception{
        OrganisationResponseDto response = new OrganisationResponseDto(1L, "TestOrg", OrganisationStatus.ACTIVE, 2L);

        when(organisationService.getOrganisation(1L)).thenReturn(response);

        mockMvc.perform(get("/api/organisations/1")).andExpect(status().isOk());

        verify(organisationService).getOrganisation(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldReturnUnauthorizedForCandidate() throws Exception{
        mockMvc.perform(get("/api/organisations/1")).andExpect(status().isUnauthorized());

        verify(organisationService, never()).getOrganisation(any());
    }
}