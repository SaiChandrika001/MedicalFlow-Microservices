package com.medicalflow.appointmentservice.service;

import com.medicalflow.appointmentservice.client.UserServiceClient;
import com.medicalflow.appointmentservice.dto.AppointmentRequest;
import com.medicalflow.appointmentservice.dto.AppointmentResponse;
import com.medicalflow.appointmentservice.dto.AppointmentStatusUpdateRequest;
import com.medicalflow.appointmentservice.dto.UserProfileResponse;
import com.medicalflow.appointmentservice.entity.Appointment;
import com.medicalflow.appointmentservice.entity.AppointmentStatus;
import com.medicalflow.appointmentservice.exception.InvalidAppointmentException;
import com.medicalflow.appointmentservice.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void createAppointmentStoresAppointmentWhenDoctorIsAvailable() {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(7L);
        request.setAppointmentDate(LocalDateTime.now().plusDays(1));
        request.setAppointmentEndDate(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setReason("Annual checkup");
        request.setNotes("Patient requires follow-up");

        when(userServiceClient.getUserById(1L)).thenReturn(new UserProfileResponse());
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatus(eq(7L), any(LocalDateTime.class), eq(AppointmentStatus.CONFIRMED)))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.createAppointment(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getDoctorId()).isEqualTo(7L);
        assertThat(response.getReason()).isEqualTo("Annual checkup");
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void getAppointmentByIdReturnsMappedResponse() {
        Appointment appointment = Appointment.builder()
                .id(5L)
                .userId(1L)
                .doctorId(7L)
                .appointmentDate(LocalDateTime.now().plusDays(2))
                .appointmentEndDate(LocalDateTime.now().plusDays(2).plusHours(1))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Consultation")
                .notes("No notes")
                .build();

        when(appointmentRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(5L, 1L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.getReason()).isEqualTo("Consultation");
    }

    @Test
    void getUserAppointmentsByStatusReturnsPagedResponses() {
        Appointment appointment = Appointment.builder()
                .id(10L)
                .userId(1L)
                .doctorId(8L)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentEndDate(LocalDateTime.now().plusDays(3).plusHours(1))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Follow-up")
                .notes("Confirmed")
                .build();

        when(appointmentRepository.findByUserIdAndStatus(1L, AppointmentStatus.CONFIRMED, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(appointment), PageRequest.of(0, 10), 1));

        Page<AppointmentResponse> page = appointmentService.getUserAppointmentsByStatus(1L, AppointmentStatus.CONFIRMED, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDoctorId()).isEqualTo(8L);
    }

    @Test
    void updateAppointmentStatusThrowsOnInvalidStatus() {
        Appointment appointment = Appointment.builder()
                .id(6L)
                .userId(1L)
                .doctorId(7L)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(appointmentRepository.findByIdAndUserId(6L, 1L)).thenReturn(Optional.of(appointment));

        AppointmentStatusUpdateRequest request = new AppointmentStatusUpdateRequest();
        request.setStatus("invalid-status");

        assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(6L, 1L, request))
                .isInstanceOf(InvalidAppointmentException.class)
                .hasMessageContaining("Invalid appointment status");
    }
}
