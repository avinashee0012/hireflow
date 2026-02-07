package com.avinashee0012.hireflow.service.impl;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avinashee0012.hireflow.config.jwt.JwtService;
import com.avinashee0012.hireflow.domain.entity.Role;
import com.avinashee0012.hireflow.domain.entity.User;
import com.avinashee0012.hireflow.dto.request.UserLoginRequestDto;
import com.avinashee0012.hireflow.dto.request.UserRegisterRequestDto;
import com.avinashee0012.hireflow.dto.response.JwtTokenResponseDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;
import com.avinashee0012.hireflow.exception.CustomDuplicateEntityException;
import com.avinashee0012.hireflow.exception.CustomInactiveUserException;
import com.avinashee0012.hireflow.mapper.UserMapper;
import com.avinashee0012.hireflow.repository.RoleRepo;
import com.avinashee0012.hireflow.repository.UserRepo;
import com.avinashee0012.hireflow.service.UserService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRegisterRequestDto request) {
        if (userRepo.existsByEmail(request.getEmail()))
            throw new CustomDuplicateEntityException("User already exists: " + request.getEmail());
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()),
                request.getOrganisationId());
        Role candidateRole = roleRepo.findByName("CANDIDATE")
                .orElseThrow(() -> new IllegalStateException("Role CANDIDATE not found"));
        user.assignRole(candidateRole);
        User savedUser = userRepo.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public JwtTokenResponseDto loginUser(UserLoginRequestDto request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        if (!user.isActive())
            throw new CustomInactiveUserException();
        String jwtToken = jwtService.generateToken(user);
        return new JwtTokenResponseDto(jwtToken, "Bearer", jwtService.expiresIn(), user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

}
