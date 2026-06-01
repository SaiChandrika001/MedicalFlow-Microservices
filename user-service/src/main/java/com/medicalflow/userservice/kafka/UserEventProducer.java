package com.medicalflow.userservice.kafka;

import com.medicalflow.userservice.event.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send("user-registered", event.getEmail(), event);
            log.info("Published user-registered event for email: {}", event.getEmail());
        } catch (Exception ex) {
            log.error("Failed to publish user-registered event", ex);
        }
    }
}
