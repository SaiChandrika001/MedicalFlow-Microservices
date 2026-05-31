package com.medicalflow.reportservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HTTP_METHOD_KEY = "httpMethod";
    public static final String REQUEST_PATH_KEY = "requestPath";
    public static final String QUERY_STRING_KEY = "queryString";
    public static final String CLIENT_IP_KEY = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = getOrCreate(request.getHeader(CORRELATION_ID_HEADER));
        String requestId = getOrCreate(request.getHeader(REQUEST_ID_HEADER));

        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(HTTP_METHOD_KEY, request.getMethod());
        MDC.put(REQUEST_PATH_KEY, request.getRequestURI());
        MDC.put(QUERY_STRING_KEY, request.getQueryString() != null ? request.getQueryString() : "");
        MDC.put(CLIENT_IP_KEY, getClientIp(request));

        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String getOrCreate(String headerValue) {
        return StringUtils.hasText(headerValue) ? headerValue : UUID.randomUUID().toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
