package com.medicalflow.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expiration;
    private String header;
    private String prefix;
    private String suffix;

    public String getToken(String token) {
        if (token != null && token.startsWith(prefix)) {
            return token.substring(prefix.length());
        }
        return token;
    }
}
