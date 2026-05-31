package com.medicalflow.appointmentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medicalflow.appointmentservice.dto.ApiResponse;
import com.medicalflow.appointmentservice.dto.AppointmentRequest;
import com.medicalflow.appointmentservice.dto.AppointmentResponse;
import com.medicalflow.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    void createAppointmentReturnsCreatedResponse() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(7L);
        request.setAppointmentDate(LocalDateTime.now().plusDays(1));
        request.setAppointmentEndDate(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setReason("Routine checkup");
        request.setNotes("Patient prefers morning");

        AppointmentResponse response = AppointmentResponse.builder()
                .id(5L)
                .userId(1L)
                .doctorId(7L)
                .reason("Routine checkup")
                .status(null)
                .build();

        when(appointmentService.createAppointment(anyLong(), any(AppointmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.doctorId").value(7));
    }

    @Test
    void getAppointmentReturnsAppointment() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .id(10L)
                .userId(1L)
                .doctorId(8L)
                .reason("Follow-up")
                .status(null)
                .build();

        when(appointmentService.getAppointmentById(10L, 1L)).thenReturn(response);

        mockMvc.perform(get("/appointments/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.reason").value("Follow-up"));
    }

    @Test
    void getUserAppointmentsReturnsPagedResult() throws Exception {
        AppointmentResponse response = AppointmentResponse.builder()
                .id(11L)
                .userId(1L)
                .doctorId(9L)
                .status(null)
                .build();

        when(appointmentService.getUserAppointments(anyLong(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/appointments")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(11));
    }
}
