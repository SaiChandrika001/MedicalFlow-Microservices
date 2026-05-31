package com.medicalflow.appointmentservice.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class UserServiceClientConfig {

    @Bean
    public RequestInterceptor feignAuthRequestInterceptor() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                if (request != null) {
                    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                    if (authorization != null && !authorization.isBlank()) {
                        template.header(HttpHeaders.AUTHORIZATION, authorization);
                    }
                }
            }
        };
    }

    @Bean
    public RequestInterceptor feignCorrelationRequestInterceptor() {
        return template -> {
            String correlationId = MDC.get("correlationId");
            String requestId = MDC.get("requestId");
            if (StringUtils.hasText(correlationId)) {
                template.header("X-Correlation-ID", correlationId);
            }
            if (StringUtils.hasText(requestId)) {
                template.header("X-Request-ID", requestId);
            }
        };
    }

    @Bean
    public ErrorDecoder userServiceErrorDecoder() {
        return new UserServiceErrorDecoder();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
