package com.medicalflow.notificationservice.kafka;

import com.medicalflow.notificationservice.dto.EventPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public EventConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "user-registered", groupId = "notification-service")
    public void handleUserRegistered(@Payload String message) {
        try {
            log.info("Received user-registered event: {}", message);
            EventPayload payload = objectMapper.readValue(message, EventPayload.class);
            
            String emailBody = String.format(
                    "Welcome %s!\n\nYour account has been successfully registered with email: %s\n\nRole: %s",
                    payload.getFullName(), payload.getEmail(), payload.getRole());
            
            sendEmail(payload.getEmail(), "Welcome to MedicalFlow", emailBody);
            log.info("Registration confirmation email sent to {}", payload.getEmail());
        } catch (Exception ex) {
            log.error("Error processing user-registered event", ex);
        }
    }

    @KafkaListener(topics = "appointment-booked", groupId = "notification-service")
    public void handleAppointmentBooked(@Payload String message) {
        try {
            log.info("Received appointment-booked event: {}", message);
            EventPayload payload = objectMapper.readValue(message, EventPayload.class);
            
            String emailBody = String.format(
                    "Your appointment has been confirmed!\n\n" +
                    "Appointment ID: %d\n" +
                    "Date: %s\n" +
                    "Reason: %s\n\n" +
                    "Please arrive 10 minutes early.",
                    payload.getAppointmentId(), payload.getAppointmentDate(), payload.getReason());
            
            sendEmail(payload.getEmail(), "Appointment Confirmation", emailBody);
            log.info("Appointment confirmation email sent");
        } catch (Exception ex) {
            log.error("Error processing appointment-booked event", ex);
        }
    }

    @KafkaListener(topics = "appointment-cancelled", groupId = "notification-service")
    public void handleAppointmentCancelled(@Payload String message) {
        try {
            log.info("Received appointment-cancelled event: {}", message);
            EventPayload payload = objectMapper.readValue(message, EventPayload.class);
            
            String emailBody = String.format(
                    "Your appointment has been cancelled.\n\n" +
                    "Appointment ID: %d\n" +
                    "Reason: %s\n\n" +
                    "If you need to reschedule, please contact support.",
                    payload.getAppointmentId(), payload.getReason());
            
            sendEmail(payload.getEmail(), "Appointment Cancelled", emailBody);
            log.info("Appointment cancellation email sent");
        } catch (Exception ex) {
            log.error("Error processing appointment-cancelled event", ex);
        }
    }

    @KafkaListener(topics = "report-uploaded", groupId = "notification-service")
    public void handleReportUploaded(@Payload String message) {
        try {
            log.info("Received report-uploaded event: {}", message);
            EventPayload payload = objectMapper.readValue(message, EventPayload.class);
            
            String emailBody = String.format(
                    "Your medical report has been uploaded.\n\n" +
                    "Report Name: %s\n" +
                    "Type: %s\n\n" +
                    "You can download it from your patient portal.",
                    payload.getReportName(), payload.getReportType());
            
            sendEmail(payload.getEmail(), "Report Available", emailBody);
            log.info("Report notification email sent");
        } catch (Exception ex) {
            log.error("Error processing report-uploaded event", ex);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@medicalflow.com");
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", to, ex);
        }
    }
}
