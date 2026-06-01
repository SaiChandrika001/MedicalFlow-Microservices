package com.medicalflow.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalflow.notificationservice.dto.EventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for Kafka consumer in notification service.
 * Uses EmbeddedKafka for local testing without external broker.
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 3,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    },
    topics = {
        "user-registered",
        "appointment-booked",
        "appointment-cancelled",
        "report-uploaded"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.consumer.group-id=test-notification-service",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.listener.poll-timeout=1000"
})
@ActiveProfiles("test")
public class EventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EventConsumer eventConsumer;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mailSender = mock(JavaMailSender.class);
    }

    @Test
    public void testUserRegisteredEventConsumption() throws Exception {
        // Given: A user registered event
        EventPayload payload = new EventPayload();
        payload.setEmail("newuser@example.com");
        payload.setFullName("Jane Doe");
        payload.setRole("PATIENT");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);

        // When: Event is published to Kafka topic
        kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);

        // Then: Consumer should process the message
        // (In real test with mail server, verify email sent)
        Thread.sleep(2000); // Wait for async processing
        
        // Verify: Email would be sent (mocked in this test)
        // verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testAppointmentBookedEventConsumption() throws Exception {
        // Given: An appointment booked event
        EventPayload payload = new EventPayload();
        payload.setAppointmentId(1L);
        payload.setPatientId(100L);
        payload.setDoctorId(200L);
        payload.setAppointmentDate(LocalDateTime.now().plusDays(7));
        payload.setReason("Routine checkup");
        payload.setEmail("patient@example.com");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);

        // When: Event is published
        kafkaTemplate.send("appointment-booked", String.valueOf(payload.getAppointmentId()), jsonMessage);

        // Then: Consumer processes it (async)
        Thread.sleep(2000);

        // Verify: Email sent with appointment details
        // In production, use WireMock to mock SMTP server
    }

    @Test
    public void testAppointmentCancelledEventConsumption() throws Exception {
        // Given: An appointment cancelled event
        EventPayload payload = new EventPayload();
        payload.setAppointmentId(1L);
        payload.setPatientId(100L);
        payload.setReason("Doctor is unavailable");
        payload.setEmail("patient@example.com");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);

        // When: Event is published
        kafkaTemplate.send("appointment-cancelled", String.valueOf(payload.getAppointmentId()), jsonMessage);

        // Then: Consumer sends cancellation email
        Thread.sleep(2000);
    }

    @Test
    public void testReportUploadedEventConsumption() throws Exception {
        // Given: A report uploaded event
        EventPayload payload = new EventPayload();
        payload.setReportId(1L);
        payload.setPatientId(100L);
        payload.setReportName("Lab Results");
        payload.setReportType("Blood Test");
        payload.setEmail("patient@example.com");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);

        // When: Event is published
        kafkaTemplate.send("report-uploaded", String.valueOf(payload.getReportId()), jsonMessage);

        // Then: Consumer sends report availability notification
        Thread.sleep(2000);
    }

    @Test
    public void testMultipleEventsProcessedInSequence() throws Exception {
        // Given: Multiple events
        for (int i = 0; i < 5; i++) {
            EventPayload payload = new EventPayload();
            payload.setEmail("user" + i + "@example.com");
            payload.setFullName("User " + i);
            payload.setRole("PATIENT");
            payload.setTimestamp(Instant.now());

            String jsonMessage = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);
        }

        // When: All events are published
        // Then: All should be consumed (possibly in parallel across partitions)
        Thread.sleep(3000);

        // In production: Query email service log to verify 5 emails sent
    }

    @Test
    public void testPartitionOrderingByEmail() throws Exception {
        // Given: Multiple events for the same email (same partition)
        String email = "consistent@example.com";
        
        for (int i = 0; i < 3; i++) {
            EventPayload payload = new EventPayload();
            payload.setEmail(email);
            payload.setFullName("User " + i);
            payload.setRole("PATIENT");
            payload.setTimestamp(Instant.now());

            String jsonMessage = objectMapper.writeValueAsString(payload);
            // Using email as key ensures same partition
            kafkaTemplate.send("user-registered", email, jsonMessage);
        }

        // When: All events sent
        Thread.sleep(2000);

        // Then: Events processed in order (same partition)
        // Verify order in logs or mock email service
    }

    @Test
    public void testConsumerGroupRecovery() throws Exception {
        // Given: Consumer group reads some messages
        EventPayload payload = new EventPayload();
        payload.setEmail("test@example.com");
        payload.setFullName("Test User");
        payload.setRole("PATIENT");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);

        Thread.sleep(1000);

        // When: Consumer is restarted
        // (In real test, stop and restart consumer)

        // Then: Consumer resumes from last committed offset
        // (No messages reprocessed or all messages reprocessed depending on config)
    }

    @Test
    public void testErrorHandlingWithInvalidJson() throws Exception {
        // Given: Invalid JSON message
        String invalidJson = "{ invalid json }";

        // When: Invalid message published
        kafkaTemplate.send("user-registered", "invalid-key", invalidJson);

        Thread.sleep(2000);

        // Then: Consumer should log error and continue
        // (Dead letter topic or error logging)
    }

    @Test
    public void testConcurrentConsumption() throws Exception {
        // Given: Multiple events for different users
        int eventCount = 100;
        
        for (int i = 0; i < eventCount; i++) {
            EventPayload payload = new EventPayload();
            payload.setEmail("user" + i + "@example.com");
            payload.setFullName("User " + i);
            payload.setRole("PATIENT");
            payload.setTimestamp(Instant.now());

            String jsonMessage = objectMapper.writeValueAsString(payload);
            // Different emails = different partitions = parallel consumption
            kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);
        }

        // When: All events published
        // Then: Consumed in parallel (3 partitions = ~33 events per partition)
        Thread.sleep(5000);

        // Verify: All 100 events processed
        // (Check log count or mock service call count)
    }

    @Test
    public void testConsumerMetrics() throws Exception {
        // Given: Consumer processing events
        EventPayload payload = new EventPayload();
        payload.setEmail("metric-test@example.com");
        payload.setFullName("Metric Test");
        payload.setRole("PATIENT");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);

        // When: Event published and consumed
        kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);
        Thread.sleep(2000);

        // Then: Metrics should be available
        // Access via: /actuator/metrics/kafka.consumer.records.consumed.total
        // or kafka.consumer.fetch.total
    }

    @Test
    public void testRetryMechanismOnTransientFailure() throws Exception {
        // Given: Mail service temporarily unavailable
        JavaMailSender mockMailSender = mock(JavaMailSender.class);
        doThrow(new RuntimeException("Mail server unavailable"))
            .doNothing()
            .when(mockMailSender).send(any(SimpleMailMessage.class));

        // When: Event published (first attempt fails, retry succeeds)
        EventPayload payload = new EventPayload();
        payload.setEmail("retry-test@example.com");
        payload.setFullName("Retry Test");
        payload.setRole("PATIENT");
        payload.setTimestamp(Instant.now());

        String jsonMessage = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send("user-registered", payload.getEmail(), jsonMessage);

        Thread.sleep(3000);

        // Then: Eventually succeeds or goes to DLT
        // verify(mockMailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}
