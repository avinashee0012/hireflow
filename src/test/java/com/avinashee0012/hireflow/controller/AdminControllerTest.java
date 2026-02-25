package com.avinashee0012.hireflow.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.Set;

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
import com.avinashee0012.hireflow.dto.request.UpdateUserRolesRequestDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldGetUsersAsSupport() throws Exception{
        Page<UserResponseDto> page = new PageImpl<>(
                List.of(new UserResponseDto(1L, "user@email.com", Set.of("CANDIDATE"), true, 10L)));

        when(adminService.getAllUsers(0)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users").param("page", "0")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(adminService).getAllUsers(0);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldGetUsersAsOrgAdmin() throws Exception{
        Page<UserResponseDto> page = Page.empty();
        when(adminService.getAllUsers(0)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk());

        verify(adminService).getAllUsers(0);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldReturnUnauthorizedForGetUsers() throws Exception{
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());

        verify(adminService, never()).getAllUsers(anyInt());
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldActivateUser() throws Exception{
        mockMvc.perform(patch("/api/admin/users/1/activate").with(csrf())).andExpect(status().isNoContent());

        verify(adminService).activateUser(1L);
    }

    @Test
    @WithMockUser(roles = "ORGADMIN") void shouldAllowOrgAdminToActivateUser() throws Exception{
        mockMvc.perform(patch("/api/admin/users/1/activate").with(csrf())).andExpect(status().isNoContent());

        verify(adminService).activateUser(1L);
    }

    @Test
    @WithMockUser(roles = "CANDIDATE") void shouldReturnUnauthorizedForActivate() throws Exception{
        mockMvc.perform(patch("/api/admin/users/1/activate").with(csrf())).andExpect(status().isUnauthorized());

        verify(adminService, never()).activateUser(any());
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldDeactivateUser() throws Exception{
        mockMvc.perform(patch("/api/admin/users/1/deactivate").with(csrf())).andExpect(status().isNoContent());

        verify(adminService).deactivateUser(1L);
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldUpdateRoles() throws Exception{
        UpdateUserRolesRequestDto request = new UpdateUserRolesRequestDto();
        request.setRoles(Set.of("RECRUITER"));

        UserResponseDto response = new UserResponseDto(1L, "user@email.com", Set.of("RECRUITER"), true, 10L);

        when(adminService.updateRoles(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("RECRUITER"));

        verify(adminService).updateRoles(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "SUPPORT") void shouldReturnBadRequestWhenRolesEmpty() throws Exception{
        UpdateUserRolesRequestDto request = new UpdateUserRolesRequestDto();
        request.setRoles(Set.of());

        mockMvc.perform(patch("/api/admin/users/1/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

        verify(adminService, never()).updateRoles(any(), any());
    }

    @Test void shouldReturnForbiddenWhenNotAuthenticated() throws Exception{
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
    }
}