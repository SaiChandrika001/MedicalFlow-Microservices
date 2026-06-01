package com.medicalflow.apigateway.filter;

import com.medicalflow.apigateway.config.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AuthenticationWebFilter {

    public JwtAuthenticationFilter(ReactiveAuthenticationManager authenticationManager, JwtProperties jwtProperties) {
        super(authenticationManager);
        setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                String authorization = exchange.getRequest().getHeaders().getFirst(jwtProperties.getHeader());
                if (authorization == null || !authorization.startsWith(jwtProperties.getPrefix())) {
                    return Mono.empty();
                }
                String token = jwtProperties.getToken(authorization).trim();
                if (token.isBlank()) {
                    return Mono.empty();
                }
                return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
            }
        });
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
    }
}
