package com.medicalflow.reportservice.service;

import com.medicalflow.reportservice.dto.ReportResponse;
import com.medicalflow.reportservice.entity.Report;
import com.medicalflow.reportservice.exception.ResourceNotFoundException;
import com.medicalflow.reportservice.repository.ReportRepository;
import com.medicalflow.reportservice.storage.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final S3Service s3Service;

    @Override
    public ReportResponse uploadReport(Long patientId, String reportName, String reportType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A valid report file is required.");
        }

        String storageKey = s3Service.uploadFile(file, patientId);
        String fileUrl = s3Service.buildObjectUrl(storageKey);

        Report report = Report.builder()
                .patientId(patientId)
                .reportName(reportName)
                .reportType(reportType)
                .storageFileName(storageKey)
                .uploadedDate(Instant.now())
                .fileUrl(fileUrl)
                .build();

        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByPatientId(Long patientId) {
        return reportRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Resource downloadReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
        return s3Service.downloadFile(report.getStorageFileName());
    }

    @Override
    public String getDownloadUrl(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
        return s3Service.generatePresignedUrl(report.getStorageFileName());
    }

    @Override
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
        s3Service.deleteFile(report.getStorageFileName());
        reportRepository.delete(report);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .patientId(report.getPatientId())
                .reportName(report.getReportName())
                .reportType(report.getReportType())
                .fileUrl(report.getFileUrl())
                .uploadedDate(report.getUploadedDate())
                .build();
    }
}
