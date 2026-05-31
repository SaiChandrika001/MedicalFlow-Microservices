package com.medicalflow.appointmentservice.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        putHeader(template, CorrelationIdFilter.CORRELATION_ID_HEADER, MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));
        putHeader(template, CorrelationIdFilter.REQUEST_ID_HEADER, MDC.get(CorrelationIdFilter.REQUEST_ID_KEY));
    }

    private void putHeader(RequestTemplate template, String headerName, String value) {
        if (StringUtils.hasText(value)) {
            template.header(headerName, value);
        }
    }
}
