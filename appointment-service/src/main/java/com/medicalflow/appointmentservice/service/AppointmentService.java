package com.medicalflow.appointmentservice.service;

import com.medicalflow.appointmentservice.dto.AppointmentRequest;
import com.medicalflow.appointmentservice.dto.AppointmentResponse;
import com.medicalflow.appointmentservice.dto.AppointmentStatusUpdateRequest;
import com.medicalflow.appointmentservice.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(Long userId, AppointmentRequest request);

    AppointmentResponse getAppointmentById(Long appointmentId, Long userId);

    Page<AppointmentResponse> getUserAppointments(Long userId, Pageable pageable);

    Page<AppointmentResponse> getDoctorAppointments(Long doctorId, Pageable pageable);

    Page<AppointmentResponse> getUserAppointmentsByStatus(Long userId, AppointmentStatus status, Pageable pageable);

    List<AppointmentResponse> getAppointmentsBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AppointmentResponse> getDoctorAppointmentsBetween(Long doctorId, LocalDateTime startDate, LocalDateTime endDate);

    AppointmentResponse updateAppointment(Long appointmentId, Long userId, AppointmentRequest request);

    AppointmentResponse updateAppointmentStatus(Long appointmentId, Long userId, AppointmentStatusUpdateRequest request);

    void cancelAppointment(Long appointmentId, Long userId, String reason);

    void deleteAppointment(Long appointmentId, Long userId);

    boolean checkDoctorAvailability(Long doctorId, LocalDateTime appointmentDate);
}
