package com.medicalflow.userservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return new NewTopic("user-registered", 3, (short) 1);
    }

    @Bean
    public NewTopic appointmentBookedTopic() {
        return new NewTopic("appointment-booked", 3, (short) 1);
    }

    @Bean
    public NewTopic appointmentCancelledTopic() {
        return new NewTopic("appointment-cancelled", 3, (short) 1);
    }

    @Bean
    public NewTopic reportUploadedTopic() {
        return new NewTopic("report-uploaded", 3, (short) 1);
    }

    @Bean
    public NewTopic userRegisteredDltTopic() {
        return new NewTopic("user-registered.DLT", 1, (short) 1);
    }

    @Bean
    public NewTopic appointmentBookedDltTopic() {
        return new NewTopic("appointment-booked.DLT", 1, (short) 1);
    }

    @Bean
    public NewTopic appointmentCancelledDltTopic() {
        return new NewTopic("appointment-cancelled.DLT", 1, (short) 1);
    }

    @Bean
    public NewTopic reportUploadedDltTopic() {
        return new NewTopic("report-uploaded.DLT", 1, (short) 1);
    }
}