package com.medicalflow.apigateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final Key signingKey;

    public JwtReactiveAuthenticationManager(@Value("${security.jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Missing JWT token"));
        }

        try {
            Jws<Claims> jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            String subject = jwsClaims.getBody().getSubject();
            if (subject == null || subject.isBlank()) {
                return Mono.error(new BadCredentialsException("JWT subject is missing"));
            }

            return Mono.just(new UsernamePasswordAuthenticationToken(subject, token,
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")));
        } catch (Exception ex) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", ex));
        }
    }
}
