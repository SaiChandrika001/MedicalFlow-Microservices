package com.medicalflow.appointmentservice.client;

import com.medicalflow.appointmentservice.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}", configuration = UserServiceClientConfig.class, fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserProfileResponse getUserById(@PathVariable("id") Long userId);

    @GetMapping("/users/by-email")
    UserProfileResponse getUserByEmail(@RequestParam("email") String email);
}
