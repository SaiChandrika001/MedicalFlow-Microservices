package com.medicalflow.appointmentservice.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMetrics {

    private final MeterRegistry meterRegistry;

    public AppointmentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordAppointmentCreated() {
        meterRegistry.counter("appointments.created.total").increment();
    }

    public void recordAppointmentCancelled() {
        meterRegistry.counter("appointments.cancelled.total").increment();
    }

    public void recordAppointmentCompleted() {
        meterRegistry.counter("appointments.completed.total").increment();
    }

    public void recordAppointmentNoShow() {
        meterRegistry.counter("appointments.no_show.total").increment();
    }

    public void recordUserServiceCall(long duration, boolean success) {
        meterRegistry.timer("user.service.call.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!success) {
            meterRegistry.counter("user.service.call.failures").increment();
        }
    }
}
