package com.medicalflow.reportservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportUploadRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be a positive number")
    private Long patientId;

    @NotBlank(message = "Report name is required")
    private String reportName;

    @NotBlank(message = "Report type is required")
    private String reportType;
}
