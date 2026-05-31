package com.medicalflow.reportservice.service;

import com.medicalflow.reportservice.dto.ReportResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {

    ReportResponse uploadReport(Long patientId, String reportName, String reportType, MultipartFile file);

    ReportResponse getReportById(Long reportId);

    String getDownloadUrl(Long reportId);

    List<ReportResponse> getReportsByPatientId(Long patientId);

    Page<ReportResponse> getAllReports(Pageable pageable);

    Resource downloadReport(Long reportId);

    void deleteReport(Long reportId);
}
