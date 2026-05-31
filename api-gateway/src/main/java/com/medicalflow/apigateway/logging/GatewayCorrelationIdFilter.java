package com.medicalflow.apigateway.logging;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class GatewayCorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HTTP_METHOD_KEY = "httpMethod";
    public static final String REQUEST_PATH_KEY = "requestPath";
    public static final String CLIENT_IP_KEY = "clientIp";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = getOrGenerate(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER));
        String requestId = getOrGenerate(exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER));

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        return chain.filter(exchange.mutate().request(request).build())
                .doOnSubscribe(subscription -> {
                    MDC.put(CORRELATION_ID_KEY, correlationId);
                    MDC.put(REQUEST_ID_KEY, requestId);
                    MDC.put(HTTP_METHOD_KEY, request.getMethodValue());
                    MDC.put(REQUEST_PATH_KEY, request.getURI().getPath());
                    MDC.put(CLIENT_IP_KEY, request.getRemoteAddress() != null
                            ? request.getRemoteAddress().getAddress().getHostAddress()
                            : "unknown");
                })
                .doFinally(signalType -> MDC.clear());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String getOrGenerate(String headerValue) {
        return StringUtils.hasText(headerValue) ? headerValue : UUID.randomUUID().toString();
    }
}
