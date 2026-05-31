package com.medicalflow.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long reportId;
    private Long patientId;
    private String reportName;
    private String reportType;
    private String fileUrl;
    private Instant uploadedDate;
}
