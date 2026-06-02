package com.medicalflow.reportservice.service;

import com.medicalflow.reportservice.kafka.ReportEventProducer;
import com.medicalflow.reportservice.entity.Report;
import com.medicalflow.reportservice.repository.ReportRepository;
import com.medicalflow.reportservice.service.ReportServiceImpl;
import com.medicalflow.reportservice.storage.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private S3Service s3Service;
    
    @Mock
    private ReportEventProducer reportEventProducer;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void uploadReportStoresMetadataAndReturnsResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "test content".getBytes());

        when(s3Service.uploadFile(file, 1L)).thenReturn("reports/1/report.pdf");
        when(s3Service.buildObjectUrl("reports/1/report.pdf")).thenReturn("https://bucket.s3.us-east-1.amazonaws.com/reports/1/report.pdf");
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reportService.uploadReport(1L, "Annual Exam", "PDF", file);

        assertThat(response).isNotNull();
        assertThat(response.getPatientId()).isEqualTo(1L);
        assertThat(response.getReportName()).isEqualTo("Annual Exam");
        assertThat(response.getFileUrl()).isEqualTo("https://bucket.s3.us-east-1.amazonaws.com/reports/1/report.pdf");
    }

    @Test
    void getDownloadUrlReturnsPresignedLink() {
        Report report = Report.builder()
                .reportId(2L)
                .storageFileName("reports/1/report.pdf")
                .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/reports/1/report.pdf")
                .uploadedDate(Instant.now())
                .patientId(1L)
                .reportName("Annual Exam")
                .reportType("PDF")
                .build();

        when(reportRepository.findById(2L)).thenReturn(Optional.of(report));
        when(s3Service.generatePresignedUrl("reports/1/report.pdf")).thenReturn("https://presigned-url");

        String url = reportService.getDownloadUrl(2L);

        assertThat(url).isEqualTo("https://presigned-url");
    }

    @Test
    void deleteReportRemovesS3ObjectAndDatabaseRecord() {
        Report report = Report.builder()
                .reportId(3L)
                .storageFileName("reports/1/report.pdf")
                .build();

        when(reportRepository.findById(3L)).thenReturn(Optional.of(report));

        reportService.deleteReport(3L);

        verify(s3Service).deleteFile("reports/1/report.pdf");
        verify(reportRepository).delete(report);
    }
}
