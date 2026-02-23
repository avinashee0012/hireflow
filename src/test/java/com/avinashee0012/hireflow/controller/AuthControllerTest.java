package com.avinashee0012.hireflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.avinashee0012.hireflow.config.jwt.JwtService;
import com.avinashee0012.hireflow.config.security.CustomUserDetailsService;
import com.avinashee0012.hireflow.dto.request.UserLoginRequestDto;
import com.avinashee0012.hireflow.dto.request.UserRegisterRequestDto;
import com.avinashee0012.hireflow.dto.response.JwtTokenResponseDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest{

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @MockitoBean
        private UserService userService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test void shouldRegisterUser() throws Exception{
                UserRegisterRequestDto request = new UserRegisterRequestDto();
                request.setEmail("test@example.com");
                request.setPassword("StrongPass123");
                request.setOrganisationId(1L);

                UserResponseDto response = new UserResponseDto(1L, "test@example.com", Set.of("CANDIDATE"), true, 1L);

                when(userService.registerUser(any())).thenReturn(response);

                mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)).with(csrf()))
                                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.roles[0]").value("CANDIDATE"))
                                .andExpect(jsonPath("$.active").value(true));

                verify(userService).registerUser(any());
        }

        @Test void shouldReturnBadRequestWhenRegisterEmailInvalid() throws Exception{
                UserRegisterRequestDto request = new UserRegisterRequestDto();
                request.setEmail("invalid-email");
                request.setPassword("StrongPass123");

                mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

                verify(userService, never()).registerUser(any());
        }

        @Test void shouldReturnBadRequestWhenPasswordTooShort() throws Exception{
                UserRegisterRequestDto request = new UserRegisterRequestDto();
                request.setEmail("test@example.com");
                request.setPassword("123");

                mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

                verify(userService, never()).registerUser(any());
        }

        @Test void shouldLoginUser() throws Exception{
                UserLoginRequestDto request = new UserLoginRequestDto();
                request.setEmail("test@example.com");
                request.setPassword("StrongPass123");

                JwtTokenResponseDto response = new JwtTokenResponseDto("jwt-token", "Bearer", 3600, "test@example.com",
                                Set.of("CANDIDATE"));

                when(userService.loginUser(any())).thenReturn(response);

                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.email").value("test@example.com"));

                verify(userService).loginUser(any());
        }

        @Test void shouldReturnBadRequestWhenLoginEmailInvalid() throws Exception{
                UserLoginRequestDto request = new UserLoginRequestDto();
                request.setEmail("invalid");
                request.setPassword("StrongPass123");

                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

                verify(userService, never()).loginUser(any());
        }

        @Test void shouldReturnBadRequestWhenLoginPasswordBlank() throws Exception{
                UserLoginRequestDto request = new UserLoginRequestDto();
                request.setEmail("test@example.com");
                request.setPassword("");

                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

                verify(userService, never()).loginUser(any());
        }
}
