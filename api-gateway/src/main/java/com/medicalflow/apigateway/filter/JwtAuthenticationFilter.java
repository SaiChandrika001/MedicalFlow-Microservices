package com.medicalflow.apigateway.filter;

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

    public JwtAuthenticationFilter(ReactiveAuthenticationManager authenticationManager) {
        super(authenticationManager);
        setServerAuthenticationConverter(new ServerAuthenticationConverter() {
            @Override
            public Mono<Authentication> convert(ServerWebExchange exchange) {
                String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authorization == null || !authorization.startsWith("Bearer ")) {
                    return Mono.empty();
                }
                String token = authorization.substring(7);
                return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
            }
        });
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
    }
}
