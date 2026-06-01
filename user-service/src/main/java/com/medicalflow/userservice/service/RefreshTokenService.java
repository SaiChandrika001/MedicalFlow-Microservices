package com.medicalflow.userservice.service;

import com.medicalflow.userservice.entity.RefreshToken;
import com.medicalflow.userservice.entity.User;
import com.medicalflow.userservice.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    private final long refreshTokenDurationDays;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${security.jwt.refresh-days:7}") long refreshTokenDurationDays) {
        this.repository = repository;
        this.refreshTokenDurationDays = refreshTokenDurationDays;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plus(refreshTokenDurationDays, ChronoUnit.DAYS));
        token.setRevoked(false);
        return repository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    public void invalidateUserTokens(User user) {
        repository.deleteAllByUser(user);
    }
}
