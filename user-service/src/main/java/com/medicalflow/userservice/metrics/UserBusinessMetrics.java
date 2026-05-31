package com.medicalflow.userservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserBusinessMetrics {
    private final Counter registerCounter;
    private final Counter loginCounter;

    public UserBusinessMetrics(MeterRegistry registry) {
        this.registerCounter = Counter.builder("user.register.count")
                .description("Number of user registrations")
                .register(registry);
        this.loginCounter = Counter.builder("user.login.count")
                .description("Number of user logins")
                .register(registry);
    }

    public void incrementRegister() { registerCounter.increment(); }
    public void incrementLogin() { loginCounter.increment(); }
}
