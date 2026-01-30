package com.avinashee0012.hireflow.service;

import com.avinashee0012.hireflow.dto.request.UserLoginRequestDto;
import com.avinashee0012.hireflow.dto.request.UserRegisterRequestDto;
import com.avinashee0012.hireflow.dto.response.JwtTokenResponseDto;
import com.avinashee0012.hireflow.dto.response.UserResponseDto;

public interface UserService {
    UserResponseDto registerUser(UserRegisterRequestDto request);
    JwtTokenResponseDto loginUser(UserLoginRequestDto request);
}
