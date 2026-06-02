package com.medicalflow.apigateway.config;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;


import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        
        // Prevent MIME type sniffing
        headers.add("X-Content-Type-Options", "nosniff");
        
        // Prevent clickjacking attacks
        headers.add("X-Frame-Options", "DENY");
        
        // Enable XSS protection
        headers.add("X-XSS-Protection", "1; mode=block");
        
        // HSTS - Force HTTPS
        headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        
        // Content Security Policy
        headers.add("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
        
        // Referrer Policy
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        headers.add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        // Remove Server header
        headers.remove("Server");

        return chain.filter(exchange);
    }
}
