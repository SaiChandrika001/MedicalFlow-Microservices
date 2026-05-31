package com.medicalflow.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.preserveHostHeader()
                                .addRequestHeader("X-Request-Source", "api-gateway")
                                .circuitBreaker(c -> c.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("http://user-service:8081"))
                .route("appointment-service", r -> r.path("/api/appointments/**")
                        .filters(f -> f.preserveHostHeader()
                                .addRequestHeader("X-Request-Source", "api-gateway")
                                .circuitBreaker(c -> c.setName("appointmentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/appointment-service")))
                        .uri("http://appointment-service:8082"))
                .route("report-service", r -> r.path("/api/reports/**")
                        .filters(f -> f.preserveHostHeader()
                                .addRequestHeader("X-Request-Source", "api-gateway")
                                .circuitBreaker(c -> c.setName("reportServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/report-service")))
                        .uri("http://report-service:8084"))
                .build();
    }
}
