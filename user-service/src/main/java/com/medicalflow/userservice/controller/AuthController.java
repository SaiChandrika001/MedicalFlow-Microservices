package com.medicalflow.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medicalflow.userservice.dto.AuthResponse;
import com.medicalflow.userservice.dto.LoginRequest;
import com.medicalflow.userservice.dto.RegisterRequest;
import com.medicalflow.userservice.entity.User;
import com.medicalflow.userservice.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import com.medicalflow.userservice.entity.RefreshToken;
import com.medicalflow.userservice.service.RefreshTokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {

        String token = refreshToken.trim();

        return refreshTokenService.findByToken(token)
                .map(rt -> {
                    if (rt.isRevoked() || rt.getExpiryDate().isBefore(java.time.Instant.now())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }

                    User user = rt.getUser();
                    String accessToken = userService.generateTokenForUser(user);

                    refreshTokenService.invalidateUserTokens(user);
                    RefreshToken newRt = refreshTokenService.createRefreshToken(user);

                    AuthResponse response =
                            new AuthResponse(accessToken, newRt.getToken());

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken) {

        String token = refreshToken.trim();

        return refreshTokenService.findByToken(token)
                .map(rt -> {
                    refreshTokenService.invalidateUserTokens(rt.getUser());
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() ->
                        ResponseEntity.noContent().build());
    }
}