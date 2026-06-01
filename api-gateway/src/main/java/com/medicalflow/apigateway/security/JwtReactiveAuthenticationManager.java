package com.medicalflow.apigateway.security;

import com.medicalflow.apigateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProperties jwtProperties;

    public JwtReactiveAuthenticationManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            return Mono.empty();
        }
        String token = authentication.getCredentials().toString();
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Collection<GrantedAuthority> authorities = extractAuthorities(claims);
            String subject = claims.getSubject();
            return Mono.just(new UsernamePasswordAuthenticationToken(subject, token, authorities));
        } catch (JwtException ex) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", ex));
        }
    }

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim == null) {
            return List.of();
        }

        List<String> roles = new ArrayList<>();
        if (rolesClaim instanceof String) {
            String[] split = ((String) rolesClaim).split(",");
            for (String role : split) {
                roles.add(role.trim());
            }
        } else if (rolesClaim instanceof List) {
            roles.addAll(((List<?>) rolesClaim).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));
        }

        return roles.stream()
                .filter(role -> !role.isBlank())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
