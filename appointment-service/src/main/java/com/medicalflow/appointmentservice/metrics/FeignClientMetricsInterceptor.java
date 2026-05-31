package com.medicalflow.appointmentservice.metrics;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class FeignClientMetricsInterceptor implements RequestInterceptor {
    private final MeterRegistry registry;

    public FeignClientMetricsInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(RequestTemplate template) {
        // Simple example: record a timer pre-request via tag of method and url
        String name = "feign.client.requests";
        Timer.builder(name)
                .description("Feign client requests")
                .tag("method", template.method())
                .tag("url", template.url())
                .register(registry)
                .record(() -> {
                    // no-op: actual timing happens at execution, this is illustrative
                });
    }
}
