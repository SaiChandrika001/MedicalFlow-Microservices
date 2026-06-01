package com.medicalflow.userservice.service;

import com.medicalflow.userservice.dto.AuthResponse;
import com.medicalflow.userservice.dto.LoginRequest;
import com.medicalflow.userservice.dto.RegisterRequest;
import com.medicalflow.userservice.entity.Role;
import com.medicalflow.userservice.entity.User;
import com.medicalflow.userservice.event.UserRegisteredEvent;
import com.medicalflow.userservice.exception.UserNotFoundException;
import com.medicalflow.userservice.kafka.UserEventProducer;
import com.medicalflow.userservice.repository.UserRepository;
import com.medicalflow.userservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserEventProducer userEventProducer;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           RefreshTokenService refreshTokenService,
                           UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userEventProducer = userEventProducer;
    }

    @Override
    @Transactional
    public AuthResponse authenticate(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            String token = jwtUtil.generateToken(authentication);
            // create refresh token
            com.medicalflow.userservice.entity.User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            com.medicalflow.userservice.entity.RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            return new AuthResponse(token, refreshToken.getToken());
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Override
    public String generateTokenForUser(User user) {
        return jwtUtil.generateTokenFromUser(user);
    }

    @Override
    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        Role role;
        try {
            role = Role.valueOf(registerRequest.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            role = Role.PATIENT;
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        
        // Publish user registered event
        UserRegisteredEvent event = new UserRegisteredEvent(
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getRole().name(),
            Instant.now()
        );
        userEventProducer.publishUserRegistered(event);
        
        return savedUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
