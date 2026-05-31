package com.medicalflow.reportservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Instant uploadedDate;

    @Column(nullable = false)
    private String storageFileName;

    @PrePersist
    public void prePersist() {
        if (uploadedDate == null) {
            uploadedDate = Instant.now();
        }
    }
}
