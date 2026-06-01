package com.medicalflow.appointmentservice.event;

import java.time.Instant;

public class AppointmentCancelledEvent {
    private Long appointmentId;
    private Long patientId;
    private String reason;
    private Instant timestamp;

    public AppointmentCancelledEvent() {}

    public AppointmentCancelledEvent(Long appointmentId, Long patientId, String reason, Instant timestamp) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
