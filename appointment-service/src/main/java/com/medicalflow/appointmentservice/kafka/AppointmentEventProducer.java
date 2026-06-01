package com.medicalflow.appointmentservice.kafka;

import com.medicalflow.appointmentservice.event.AppointmentBookedEvent;
import com.medicalflow.appointmentservice.event.AppointmentCancelledEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventProducer.class);
    private final KafkaTemplate<String, AppointmentBookedEvent> bookedTemplate;
    private final KafkaTemplate<String, AppointmentCancelledEvent> cancelledTemplate;

    public AppointmentEventProducer(
            KafkaTemplate<String, AppointmentBookedEvent> bookedTemplate,
            KafkaTemplate<String, AppointmentCancelledEvent> cancelledTemplate) {
        this.bookedTemplate = bookedTemplate;
        this.cancelledTemplate = cancelledTemplate;
    }

    public void publishAppointmentBooked(AppointmentBookedEvent event) {
        try {
            bookedTemplate.send("appointment-booked", String.valueOf(event.getAppointmentId()), event);
            log.info("Published appointment-booked event for appointment: {}", event.getAppointmentId());
        } catch (Exception ex) {
            log.error("Failed to publish appointment-booked event", ex);
        }
    }

    public void publishAppointmentCancelled(AppointmentCancelledEvent event) {
        try {
            cancelledTemplate.send("appointment-cancelled", String.valueOf(event.getAppointmentId()), event);
            log.info("Published appointment-cancelled event for appointment: {}", event.getAppointmentId());
        } catch (Exception ex) {
            log.error("Failed to publish appointment-cancelled event", ex);
        }
    }
}
