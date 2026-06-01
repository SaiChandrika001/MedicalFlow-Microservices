package com.medicalflow.reportservice.kafka;

import com.medicalflow.reportservice.event.ReportUploadedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ReportEventProducer.class);
    private final KafkaTemplate<String, ReportUploadedEvent> kafkaTemplate;

    public ReportEventProducer(KafkaTemplate<String, ReportUploadedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReportUploaded(ReportUploadedEvent event) {
        try {
            kafkaTemplate.send("report-uploaded", String.valueOf(event.getReportId()), event);
            log.info("Published report-uploaded event for report: {}", event.getReportId());
        } catch (Exception ex) {
            log.error("Failed to publish report-uploaded event", ex);
        }
    }
}
