package com.medicalflow.reportservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalflow.reportservice.dto.ReportResponse;
import com.medicalflow.reportservice.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    void getReportReturnsMetadata() throws Exception {
        ReportResponse response = ReportResponse.builder()
                .reportId(5L)
                .patientId(1L)
                .reportName("Test Report")
                .reportType("PDF")
                .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/report.pdf")
                .uploadedDate(Instant.now())
                .build();

        when(reportService.getReportsByPatientId(5L)).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reportId").value(5));
    }

    @Test
    void getReportDownloadUrlReturnsUrl() throws Exception {
        when(reportService.getDownloadUrl(7L)).thenReturn("https://presigned-url");

        mockMvc.perform(get("/reports/download-url/7")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://presigned-url"));
    }

    @Test
    void deleteReportReturnsSuccess() throws Exception {
        doNothing().when(reportService).deleteReport(anyLong());

        mockMvc.perform(delete("/reports/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
