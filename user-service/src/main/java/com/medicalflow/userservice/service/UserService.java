package com.medicalflow.userservice.service;

import com.medicalflow.userservice.dto.AuthResponse;
import com.medicalflow.userservice.dto.LoginRequest;
import com.medicalflow.userservice.dto.RegisterRequest;
import com.medicalflow.userservice.entity.User;

public interface UserService {
    AuthResponse authenticate(LoginRequest loginRequest);
    User register(RegisterRequest registerRequest);
    User findByEmail(String email);
    User findById(Long id);
}
