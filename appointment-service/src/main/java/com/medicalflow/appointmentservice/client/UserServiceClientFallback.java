package com.medicalflow.appointmentservice.client;

import com.medicalflow.appointmentservice.dto.UserProfileResponse;
import com.medicalflow.appointmentservice.exception.UserServiceClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public UserProfileResponse getUserById(Long userId) {
        String message = "User service fallback activated for getUserById(" + userId + ")";
        log.error(message);
        throw new UserServiceClientException(message);
    }

    @Override
    public UserProfileResponse getUserByEmail(String email) {
        String message = "User service fallback activated for getUserByEmail(" + email + ")";
        log.error(message);
        throw new UserServiceClientException(message);
    }
}
