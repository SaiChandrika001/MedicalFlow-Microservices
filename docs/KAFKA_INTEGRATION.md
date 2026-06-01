# Kafka Integration Guide

## Overview
This guide explains how Kafka is integrated into the MedicalFlow microservices architecture for event-driven communication.

## Topics

### 1. **user-registered**
- **Producer**: user-service (UserEventProducer)
- **Consumer**: notification-service (EventConsumer)
- **Event**: UserRegisteredEvent
- **Payload**:
  ```json
  {
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "PATIENT",
    "timestamp": "2026-05-31T12:00:00Z"
  }
  ```
- **Purpose**: Send welcome emails to newly registered users

### 2. **appointment-booked**
- **Producer**: appointment-service (AppointmentEventProducer)
- **Consumer**: notification-service (EventConsumer)
- **Event**: AppointmentBookedEvent
- **Payload**:
  ```json
  {
    "appointmentId": 1,
    "patientId": 100,
    "doctorId": 200,
    "appointmentDate": "2026-06-15T10:00:00",
    "reason": "Routine checkup",
    "timestamp": "2026-05-31T12:00:00Z"
  }
  ```
- **Purpose**: Send appointment confirmation emails

### 3. **appointment-cancelled**
- **Producer**: appointment-service (AppointmentEventProducer)
- **Consumer**: notification-service (EventConsumer)
- **Event**: AppointmentCancelledEvent
- **Payload**:
  ```json
  {
    "appointmentId": 1,
    "patientId": 100,
    "reason": "Doctor unavailable",
    "timestamp": "2026-05-31T12:00:00Z"
  }
  ```
- **Purpose**: Send appointment cancellation notification emails

### 4. **report-uploaded**
- **Producer**: report-service (ReportEventProducer)
- **Consumer**: notification-service (EventConsumer)
- **Event**: ReportUploadedEvent
- **Payload**:
  ```json
  {
    "reportId": 1,
    "patientId": 100,
    "reportName": "Lab Results",
    "reportType": "Blood Test",
    "timestamp": "2026-05-31T12:00:00Z"
  }
  ```
- **Purpose**: Notify patients when medical reports become available

## Dead Letter Topics (DLT)

Each topic has a corresponding DLT for failed messages:
- `user-registered.DLT`
- `appointment-booked.DLT`
- `appointment-cancelled.DLT`
- `report-uploaded.DLT`

**Configuration**: Automatically configured with Spring Kafka retry template (3 retries with exponential backoff).

## Environment Configuration

### Local Development (localhost)
```properties
spring.kafka.bootstrap-servers=localhost:9092
```

### Docker Environment
```properties
spring.kafka.bootstrap-servers=kafka:9092
```

### Production (Confluent Cloud / AWS MSK)
```properties
spring.kafka.bootstrap-servers=kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092
spring.kafka.security.protocol=SASL_SSL
spring.kafka.sasl.mechanism=PLAIN
spring.kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="key" password="secret";
```

## Producer Usage Example

### In user-service (UserEventProducer)
```java
UserRegisteredEvent event = new UserRegisteredEvent(
    email,
    fullName,
    role.name(),
    Instant.now()
);
userEventProducer.publishUserRegistered(event);
```

### Integration Point in UserServiceImpl.register()
```java
@Override
@Transactional
public User register(RegisterRequest registerRequest) {
    // ... registration logic ...
    User savedUser = userRepository.save(user);
    
    // Publish user registered event
    UserRegisteredEvent event = new UserRegisteredEvent(
        savedUser.getEmail(),
        savedUser.getFullName(),
        savedUser.getRole().name(),
        Instant.now()
    );
    userEventProducer.publishUserRegistered(event);
    
    return savedUser;
}
```

## Consumer Usage Example

### In notification-service (EventConsumer)
```java
@KafkaListener(topics = "user-registered", groupId = "notification-service")
public void handleUserRegistered(@Payload String message) {
    try {
        EventPayload payload = objectMapper.readValue(message, EventPayload.class);
        String emailBody = String.format(
            "Welcome %s!\n\nYour account has been successfully registered.",
            payload.getFullName());
        sendEmail(payload.getEmail(), "Welcome to MedicalFlow", emailBody);
    } catch (Exception ex) {
        log.error("Error processing user-registered event", ex);
    }
}
```

## Key Features

1. **JSON Serialization**: All events are serialized as JSON for language interoperability
2. **Partition Strategy**: Topics use key-based partitioning (email, appointmentId, reportId) for ordered message delivery
3. **Retention**: 
   - `user-registered`: 1 day (86400000 ms)
   - `appointment-booked/cancelled`: 7 days (604800000 ms)
   - `report-uploaded`: 30 days (2592000000 ms)
4. **Partitions**: 3 partitions per topic for parallelism
5. **Replication**: Factor of 1 (suitable for development; upgrade to 3 for production)
6. **Error Handling**: Automatic retries (3 times) with exponential backoff

## Testing Events

### Produce a test event using Kafka CLI
```bash
docker-compose exec kafka kafka-console-producer \
  --broker-list localhost:9092 \
  --topic user-registered

# Paste JSON message:
{"email":"test@example.com","fullName":"Test User","role":"PATIENT","timestamp":"2026-05-31T12:00:00Z"}
```

### Consume messages from a topic
```bash
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-registered \
  --from-beginning
```

### List all topics
```bash
docker-compose exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

## Monitoring & Metrics

Spring Kafka provides Micrometer metrics:
- `kafka.producer.record.send.total`
- `kafka.producer.record.error.total`
- `kafka.consumer.records.consumed.total`
- `kafka.consumer.fetch.total`

Access via: `/actuator/metrics/kafka.*`

## Future Enhancements

1. **Message Routing**: Implement content-based routing
2. **Event Sourcing**: Store all events in event store for audit trails
3. **CQRS**: Separate read/write models using Kafka events
4. **Real-time Analytics**: Stream appointment/report events to Kafka Stream aggregations
5. **Schema Registry**: Use Confluent Schema Registry for schema versioning
6. **Exactly-Once Semantics**: Enable transactional producers for idempotency

## Troubleshooting

### Topic not created automatically
Ensure `KafkaConfig` beans are initialized:
```java
@Bean
public NewTopic userRegisteredTopic() {
    return new NewTopic("user-registered", 3, (short) 1);
}
```

### Consumer not receiving messages
1. Check broker connectivity: `docker-compose logs kafka`
2. Verify consumer group: `kafka-consumer-groups --bootstrap-server localhost:9092 --list`
3. Check topic lag: `kafka-consumer-groups --bootstrap-server localhost:9092 --group notification-service --describe`

### Messages in DLT
1. Check application logs for error details
2. Investigate failed message in DLT
3. Fix underlying issue and reprocess from DLT

## References
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Confluent Kafka Best Practices](https://docs.confluent.io/platform/current/installation/index.html)
