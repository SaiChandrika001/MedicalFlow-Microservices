package com.medicalflow.reportservice.event;

import java.time.Instant;

public class ReportUploadedEvent {
    private Long reportId;
    private Long patientId;
    private String reportName;
    private String reportType;
    private Instant timestamp;

    public ReportUploadedEvent() {}

    public ReportUploadedEvent(Long reportId, Long patientId, String reportName, String reportType, Instant timestamp) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.reportName = reportName;
        this.reportType = reportType;
        this.timestamp = timestamp;
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
