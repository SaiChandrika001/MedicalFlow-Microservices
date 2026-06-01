package com.medicalflow.reportservice.controller;

import com.medicalflow.reportservice.dto.ApiResponse;
import com.medicalflow.reportservice.dto.ReportResponse;
import com.medicalflow.reportservice.dto.ReportUploadRequest;
import com.medicalflow.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/reports")
@Tag(name = "Report Service", description = "Upload, view, download, and delete medical reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a medical report", description = "Uploads a report file and stores metadata for a patient")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> uploadReport(
            @Valid @ModelAttribute ReportUploadRequest request,
            @RequestParam("file") MultipartFile file) {

        ReportResponse response = reportService.uploadReport(
                request.getPatientId(),
                request.getReportName(),
                request.getReportType(),
                file);

        return ResponseEntity.ok(new ApiResponse<>(true, "Report uploaded successfully", response));
    }

    @GetMapping("/report/{reportId}")
    @Operation(summary = "Get report metadata", description = "Returns metadata for a single report")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','PATIENT')")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(@PathVariable Long reportId) {
        ReportResponse response = reportService.getReportById(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Report retrieved", response));
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get reports by patient", description = "Returns all reports for a given patient")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','PATIENT')")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsByPatient(@PathVariable Long patientId) {
        List<ReportResponse> reports = reportService.getReportsByPatientId(patientId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Patient reports retrieved", reports));
    }

    @GetMapping
    @Operation(summary = "List all reports", description = "Lists all reports with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reports retrieved", reports));
    }

    @GetMapping("/download/{reportId}")
    @Operation(summary = "Download report file", description = "Downloads the report file for a given report ID")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','PATIENT')")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long reportId) {
        Resource resource = reportService.downloadReport(reportId);
        String fileName = resource.getFilename();
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        try {
            Path path = Paths.get(resource.getURI());
            String detectedType = Files.probeContentType(path);
            if (detectedType != null) {
                contentType = detectedType;
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/download-url/{reportId}")
    @Operation(summary = "Get presigned download URL", description = "Returns a short-lived AWS S3 presigned URL for a report file")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN','PATIENT')")
    public ResponseEntity<ApiResponse<String>> getReportDownloadUrl(@PathVariable Long reportId) {
        String downloadUrl = reportService.getDownloadUrl(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Presigned download URL generated", downloadUrl));
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete report", description = "Deletes a report and its stored file")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Report deleted successfully", null));
    }
}
