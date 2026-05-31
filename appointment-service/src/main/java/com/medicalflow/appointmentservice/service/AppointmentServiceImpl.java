package com.medicalflow.appointmentservice.service;

import com.medicalflow.appointmentservice.client.UserServiceClient;
import com.medicalflow.appointmentservice.dto.AppointmentRequest;
import com.medicalflow.appointmentservice.dto.AppointmentResponse;
import com.medicalflow.appointmentservice.dto.AppointmentStatusUpdateRequest;
import com.medicalflow.appointmentservice.entity.Appointment;
import com.medicalflow.appointmentservice.entity.AppointmentStatus;
import com.medicalflow.appointmentservice.dto.UserProfileResponse;
import com.medicalflow.appointmentservice.exception.InvalidAppointmentException;
import com.medicalflow.appointmentservice.exception.ResourceNotFoundException;
import com.medicalflow.appointmentservice.exception.UserServiceClientException;
import com.medicalflow.appointmentservice.repository.AppointmentRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public AppointmentResponse createAppointment(Long userId, AppointmentRequest request) {
        log.info("Creating appointment for user: {}", userId);

        // Verify user exists
        try {
            getUserProfile(userId);
        } catch (Exception e) {
            log.error("User not found: {}", userId, e);
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Validate appointment dates
        if (request.getAppointmentDate().isAfter(request.getAppointmentEndDate())) {
            throw new InvalidAppointmentException("Appointment end date must be after start date");
        }

        // Check doctor availability
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatus(
                request.getDoctorId(), request.getAppointmentDate(), AppointmentStatus.CONFIRMED)) {
            throw new InvalidAppointmentException("Doctor is not available at the requested time");
        }

        Appointment appointment = Appointment.builder()
                .userId(userId)
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .appointmentEndDate(request.getAppointmentEndDate())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId, Long userId) {
        log.info("Fetching appointment: {} for user: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId + " for user: " + userId));

        return mapToResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getUserAppointments(Long userId, Pageable pageable) {
        log.info("Fetching appointments for user: {}", userId);
        return appointmentRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(Long doctorId, Pageable pageable) {
        log.info("Fetching appointments for doctor: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getUserAppointmentsByStatus(Long userId, AppointmentStatus status, Pageable pageable) {
        log.info("Fetching appointments for user: {} with status: {}", userId, status);
        return appointmentRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching appointments between {} and {}", startDate, endDate);
        return appointmentRepository.findAppointmentsBetween(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointmentsBetween(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching appointments for doctor: {} between {} and {}", doctorId, startDate, endDate);
        return appointmentRepository.findDoctorAppointmentsBetween(doctorId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse updateAppointment(Long appointmentId, Long userId, AppointmentRequest request) {
        log.info("Updating appointment: {} for user: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && 
            appointment.getStatus() != AppointmentStatus.RESCHEDULED) {
            throw new InvalidAppointmentException("Cannot update appointment with status: " + appointment.getStatus());
        }

        if (request.getAppointmentDate().isAfter(request.getAppointmentEndDate())) {
            throw new InvalidAppointmentException("Appointment end date must be after start date");
        }

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentEndDate(request.getAppointmentEndDate());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment updated: {}", appointmentId);
        return mapToResponse(updated);
    }

    @Override
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, Long userId, AppointmentStatusUpdateRequest request) {
        log.info("Updating status for appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(request.getStatus().toUpperCase());
            appointment.setStatus(newStatus);
            Appointment updated = appointmentRepository.save(appointment);
            log.info("Appointment status updated to: {}", newStatus);
            return mapToResponse(updated);
        } catch (IllegalArgumentException ex) {
            throw new InvalidAppointmentException("Invalid appointment status: " + request.getStatus());
        }
    }

    @Override
    public void cancelAppointment(Long appointmentId, Long userId, String reason) {
        log.info("Cancelling appointment: {} for user: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        appointment.setNotes(reason != null ? reason : appointment.getNotes());
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled: {}", appointmentId);
    }

    @Override
    public void deleteAppointment(Long appointmentId, Long userId) {
        log.info("Deleting appointment: {} for user: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.IN_PROGRESS || 
            appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Cannot delete an in-progress or completed appointment");
        }

        appointmentRepository.delete(appointment);
        log.info("Appointment deleted: {}", appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkDoctorAvailability(Long doctorId, LocalDateTime appointmentDate) {
        return !appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatus(
                doctorId, appointmentDate, AppointmentStatus.CONFIRMED);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserProfileFallback")
    @Retry(name = "userService")
    @TimeLimiter(name = "userService")
    @Bulkhead(name = "userService", type = Bulkhead.Type.SEMAPHORE)
    public CompletableFuture<UserProfileResponse> getUserProfileAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> userServiceClient.getUserById(userId));
    }

    public UserProfileResponse getUserProfileFallback(Long userId, Throwable throwable) {
        String message = "User service fallback activated for userId=" + userId;
        log.error(message, throwable);
        throw new UserServiceClientException(message, throwable);
    }

    private UserProfileResponse getUserProfile(Long userId) {
        try {
            return getUserProfileAsync(userId).join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            if (cause instanceof UserServiceClientException) {
                throw (UserServiceClientException) cause;
            }
            throw new ResourceNotFoundException("User service unavailable for id: " + userId);
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUserId())
                .doctorId(appointment.getDoctorId())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentEndDate(appointment.getAppointmentEndDate())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .cancelledAt(appointment.getCancelledAt())
                .build();
    }
}
