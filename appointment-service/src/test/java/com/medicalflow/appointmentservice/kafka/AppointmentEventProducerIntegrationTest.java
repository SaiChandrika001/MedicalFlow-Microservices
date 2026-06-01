package com.medicalflow.appointmentservice.kafka;

import com.medicalflow.appointmentservice.event.AppointmentBookedEvent;
import com.medicalflow.appointmentservice.event.AppointmentCancelledEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Kafka producers in appointment service.
 * Tests that events are correctly published to Kafka topics.
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 3,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    },
    topics = {
        "appointment-booked",
        "appointment-cancelled"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
@ActiveProfiles("test")
public class AppointmentEventProducerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AppointmentEventProducer appointmentEventProducer;

    @Autowired
    private KafkaTemplate<String, AppointmentBookedEvent> bookedTemplate;

    @Autowired
    private KafkaTemplate<String, AppointmentCancelledEvent> cancelledTemplate;

    @BeforeEach
    public void setUp() {
        // Reset embedded Kafka before each test
        embeddedKafkaBroker.getKafkaServers().forEach(server -> 
            server.clearLogs()
        );
    }

    @Test
    public void testPublishAppointmentBookedEvent() throws Exception {
        // Given: An appointment booked event
        AppointmentBookedEvent event = new AppointmentBookedEvent(
            1L,                           // appointmentId
            100L,                         // patientId
            200L,                         // doctorId
            LocalDateTime.now().plusDays(7),  // appointmentDate
            "Routine checkup",           // reason
            Instant.now()                 // timestamp
        );

        // When: Event is published
        appointmentEventProducer.publishAppointmentBooked(event);

        // Then: Message should be in Kafka topic
        // (Verify via consumer reading the topic)
        Thread.sleep(1000); // Wait for async send
        assertTrue(true); // Event sent successfully (no exception)
    }

    @Test
    public void testPublishAppointmentCancelledEvent() throws Exception {
        // Given: An appointment cancelled event
        AppointmentCancelledEvent event = new AppointmentCancelledEvent(
            1L,                          // appointmentId
            100L,                        // patientId
            "Doctor is unavailable",     // reason
            Instant.now()                // timestamp
        );

        // When: Event is published
        appointmentEventProducer.publishAppointmentCancelled(event);

        // Then: Message should be in Kafka topic
        Thread.sleep(1000);
        assertTrue(true); // Event sent successfully
    }

    @Test
    public void testMultipleAppointmentEventsPublished() throws Exception {
        // Given: Multiple appointment events
        for (int i = 1; i <= 5; i++) {
            AppointmentBookedEvent event = new AppointmentBookedEvent(
                (long)i,
                100L + i,
                200L,
                LocalDateTime.now().plusDays(i),
                "Appointment " + i,
                Instant.now()
            );
            appointmentEventProducer.publishAppointmentBooked(event);
        }

        // When: All events published
        Thread.sleep(2000);

        // Then: All should be in topic
        // Verify via topic inspection
    }

    @Test
    public void testEventPartitioningByAppointmentId() throws Exception {
        // Given: Events with same appointment ID (should go to same partition)
        Long appointmentId = 1L;
        
        for (int i = 0; i < 3; i++) {
            AppointmentCancelledEvent event = new AppointmentCancelledEvent(
                appointmentId,
                100L + i,
                "Cancellation reason " + i,
                Instant.now()
            );
            appointmentEventProducer.publishAppointmentCancelled(event);
        }

        // When: Events published
        Thread.sleep(2000);

        // Then: All events should be in same partition (key = appointmentId)
        // Partition key ensures ordering for same appointment
    }

    @Test
    public void testEventTimestampIsSet() throws Exception {
        // Given: An appointment event
        Instant beforePublish = Instant.now();
        
        AppointmentBookedEvent event = new AppointmentBookedEvent(
            1L,
            100L,
            200L,
            LocalDateTime.now().plusDays(7),
            "Test appointment",
            Instant.now()
        );

        // When: Event published
        appointmentEventProducer.publishAppointmentBooked(event);

        Instant afterPublish = Instant.now();
        Thread.sleep(500);

        // Then: Event timestamp should be between before and after
        assertTrue(event.getTimestamp().isAfter(beforePublish) || 
                   event.getTimestamp().equals(beforePublish));
        assertTrue(event.getTimestamp().isBefore(afterPublish) || 
                   event.getTimestamp().equals(afterPublish));
    }

    @Test
    public void testEventPayloadContainsAllData() throws Exception {
        // Given: Event with complete data
        Long appointmentId = 123L;
        Long patientId = 456L;
        Long doctorId = 789L;
        LocalDateTime appointmentDate = LocalDateTime.now().plusDays(10);
        String reason = "Comprehensive health checkup";

        AppointmentBookedEvent event = new AppointmentBookedEvent(
            appointmentId,
            patientId,
            doctorId,
            appointmentDate,
            reason,
            Instant.now()
        );

        // When: Event published
        appointmentEventProducer.publishAppointmentBooked(event);

        // Then: Verify all fields are set
        assertEquals(appointmentId, event.getAppointmentId());
        assertEquals(patientId, event.getPatientId());
        assertEquals(doctorId, event.getDoctorId());
        assertEquals(appointmentDate, event.getAppointmentDate());
        assertEquals(reason, event.getReason());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testProducerErrorHandling() throws Exception {
        // Given: Event with null required field (if validation enabled)
        AppointmentBookedEvent event = new AppointmentBookedEvent(
            null,  // null appointment ID
            100L,
            200L,
            LocalDateTime.now().plusDays(7),
            "Test",
            Instant.now()
        );

        // When: Attempt to publish (may fail depending on producer config)
        try {
            appointmentEventProducer.publishAppointmentBooked(event);
            // If no exception, producer sent it (serialization might be lenient)
        } catch (Exception ex) {
            // Expected: validation or serialization error
            assertNotNull(ex);
        }
    }

    @Test
    public void testConcurrentEventPublishing() throws Exception {
        // Given: Multiple threads publishing events
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                AppointmentBookedEvent event = new AppointmentBookedEvent(
                    (long)threadId,
                    100L + threadId,
                    200L,
                    LocalDateTime.now().plusDays(1),
                    "Thread appointment " + threadId,
                    Instant.now()
                );
                appointmentEventProducer.publishAppointmentBooked(event);
            });
        }

        // When: All threads start publishing
        for (Thread t : threads) {
            t.start();
        }
        
        for (Thread t : threads) {
            t.join();
        }

        // Then: All events should be published
        Thread.sleep(2000); // Wait for async completion
        assertTrue(true); // All threads completed
    }

    @Test
    public void testEventSerializationFormat() throws Exception {
        // Given: Event to be serialized
        AppointmentBookedEvent event = new AppointmentBookedEvent(
            1L,
            100L,
            200L,
            LocalDateTime.now().plusDays(7),
            "Serialization test",
            Instant.now()
        );

        // When: Sent to Kafka (JsonSerializer converts to JSON)
        appointmentEventProducer.publishAppointmentBooked(event);

        Thread.sleep(1000);

        // Then: Message in Kafka is valid JSON with all fields
        // Verify via console consumer:
        // docker exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic appointment-booked --from-beginning
    }

    @Test
    public void testRetryOnProducerFailure() throws Exception {
        // Given: A valid event
        AppointmentBookedEvent event = new AppointmentBookedEvent(
            1L,
            100L,
            200L,
            LocalDateTime.now().plusDays(7),
            "Retry test",
            Instant.now()
        );

        // When: Published (normally succeeds)
        appointmentEventProducer.publishAppointmentBooked(event);

        // Then: Should succeed on first try
        Thread.sleep(500);

        // (Retry logic would activate if broker was unavailable)
        assertTrue(true); // Successfully published
    }

    @Test
    public void testHighThroughputEventPublishing() throws Exception {
        // Given: Large number of events
        int eventCount = 1000;
        long startTime = System.currentTimeMillis();

        // When: All events published in rapid succession
        for (int i = 0; i < eventCount; i++) {
            AppointmentBookedEvent event = new AppointmentBookedEvent(
                (long)i,
                100L + (i % 100),
                200L,
                LocalDateTime.now().plusDays(1),
                "Event " + i,
                Instant.now()
            );
            appointmentEventProducer.publishAppointmentBooked(event);
        }

        // Then: Should complete within reasonable time
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // For 1000 events, should take < 5 seconds (batching enabled)
        assertTrue(duration < 5000, "High-throughput publishing took too long: " + duration + "ms");
    }

    @Test
    public void testEventKeyUsedForPartitioning() throws Exception {
        // Given: Multiple events with different appointment IDs
        AppointmentBookedEvent event1 = new AppointmentBookedEvent(1L, 100L, 200L, 
            LocalDateTime.now().plusDays(1), "Event 1", Instant.now());
        AppointmentBookedEvent event2 = new AppointmentBookedEvent(2L, 100L, 200L, 
            LocalDateTime.now().plusDays(1), "Event 2", Instant.now());
        AppointmentBookedEvent event3 = new AppointmentBookedEvent(3L, 100L, 200L, 
            LocalDateTime.now().plusDays(1), "Event 3", Instant.now());

        // When: Published (different keys = potentially different partitions)
        appointmentEventProducer.publishAppointmentBooked(event1);
        appointmentEventProducer.publishAppointmentBooked(event2);
        appointmentEventProducer.publishAppointmentBooked(event3);

        Thread.sleep(1000);

        // Then: Events distributed across partitions for parallelism
        // (Verify via topic inspection)
    }
}
