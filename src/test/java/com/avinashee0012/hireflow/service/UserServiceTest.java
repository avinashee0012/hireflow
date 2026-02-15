package com.avinashee0012.hireflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.avinashee0012.hireflow.config.jwt.JwtService;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.UserLoginRequestDto;
import com.avinashee0012.hireflow.dto.request.UserRegisterRequestDto;
import com.avinashee0012.hireflow.dto.response.JwtTokenResponseDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.exception.CustomInactiveUserException;
import com.avinashee0012.hireflow.mapper.UserMapper;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest{

    @Mock
    private UserRepo userRepo;
    @Mock
    private RoleRepo roleRepo;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterRequestDto registerRequest;

    private UserLoginRequestDto loginRequest;

    @BeforeEach 
    void setUp(){
        registerRequest = new UserRegisterRequestDto();
        registerRequest.setEmail("test@email.com");
        registerRequest.setPassword("password123");
        registerRequest.setOrganisationId(null);

        loginRequest = new UserLoginRequestDto();
        loginRequest.setEmail("test@email.com");
        loginRequest.setPassword("password123");
    }

    @Test 
    void shouldRegisterUserSuccessfully(){
        Role candidateRole = new Role("CANDIDATE");
        when(userRepo.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPass");
        when(roleRepo.findByName("CANDIDATE")).thenReturn(Optional.of(candidateRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(new UserResponseDto(1L, registerRequest.getEmail(), Set.of("CANDIDATE"), true, null));
        UserResponseDto response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals("test@email.com", response.getEmail());

        verify(userRepo).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(roleRepo).findByName("CANDIDATE");
        verify(userRepo).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test 
    void shouldThrowIfCandidateRoleMissing(){
        when(userRepo.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPass");
        when(roleRepo.findByName("CANDIDATE")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.registerUser(registerRequest));

        verify(userRepo, never()).save(any());
    }

    @Test 
    void shouldLoginSuccessfully(){
        Role role = new Role("CANDIDATE");
        User user = new User("test@email.com", "encodedPass", null);
        user.assignRole(role);

        when(authenticationManager.authenticate(any())).thenReturn(null); // authentication passes
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mocked-token");
        when(jwtService.expiresIn()).thenReturn(3600L);
        JwtTokenResponseDto response = userService.loginUser(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@email.com", response.getEmail());
        assertTrue(response.getRoles().contains("CANDIDATE"));

        verify(authenticationManager).authenticate(any());
        verify(userRepo).findByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(user);
    }

    @Test 
    void shouldThrowIfAuthenticatedUserNotFound(){
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.loginUser(loginRequest));
    }

    @Test 
    void shouldThrowIfUserInactive(){
        User user = new User("test@email.com", "encodedPass", null);
        user.deactivate();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepo.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(CustomInactiveUserException.class, () -> userService.loginUser(loginRequest));

        verify(jwtService, never()).generateToken(any());
    }

    @Test 
    void shouldPropagateAuthenticationFailure(){
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.loginUser(loginRequest));

        verify(userRepo, never()).findByEmail(any());
    }

}
