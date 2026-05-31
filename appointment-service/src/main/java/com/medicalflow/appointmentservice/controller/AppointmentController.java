package com.medicalflow.appointmentservice.controller;

import com.medicalflow.appointmentservice.dto.ApiResponse;
import com.medicalflow.appointmentservice.dto.AppointmentRequest;
import com.medicalflow.appointmentservice.dto.AppointmentResponse;
import com.medicalflow.appointmentservice.dto.AppointmentStatusUpdateRequest;
import com.medicalflow.appointmentservice.entity.AppointmentStatus;
import com.medicalflow.appointmentservice.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment Management", description = "APIs for managing medical appointments")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Create a new appointment", description = "Creates a new appointment for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Appointment created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        log.info("Creating appointment");
        Long userId = getCurrentUserId();
        AppointmentResponse appointment = appointmentService.createAppointment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Appointment created successfully", appointment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Get appointment by ID", description = "Retrieves appointment details by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable Long id) {
        log.info("Fetching appointment: {}", id);
        Long userId = getCurrentUserId();
        AppointmentResponse appointment = appointmentService.getAppointmentById(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment retrieved successfully", appointment));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Get user appointments", description = "Retrieves all appointments for the authenticated user with pagination")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getUserAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Fetching user appointments - page: {}, size: {}", page, size);
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AppointmentResponse> appointments = appointmentService.getUserAppointments(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointments retrieved successfully", appointments));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Get doctor appointments", description = "Retrieves all appointments for a specific doctor")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching doctor appointments - doctorId: {}", doctorId);
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Doctor appointments retrieved successfully", appointments));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Get appointments by status", description = "Retrieves appointments filtered by status")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointmentsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching appointments with status: {}", status);
        Long userId = getCurrentUserId();
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointments = appointmentService.getUserAppointmentsByStatus(userId, appointmentStatus, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointments retrieved successfully", appointments));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Update appointment", description = "Updates appointment details")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        log.info("Updating appointment: {}", id);
        Long userId = getCurrentUserId();
        AppointmentResponse appointment = appointmentService.updateAppointment(id, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment updated successfully", appointment));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    @Operation(summary = "Update appointment status", description = "Updates the status of an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        log.info("Updating appointment status: {}", id);
        Long userId = getCurrentUserId();
        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment status updated successfully", appointment));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Cancel appointment", description = "Cancels an existing appointment")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling appointment: {}", id);
        Long userId = getCurrentUserId();
        appointmentService.cancelAppointment(id, userId, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment cancelled successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Delete appointment", description = "Deletes an appointment")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long id) {
        log.info("Deleting appointment: {}", id);
        Long userId = getCurrentUserId();
        appointmentService.deleteAppointment(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Appointment deleted successfully", null));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // In a real scenario, extract user ID from JWT token
        // For now, returning a placeholder
        return 1L; // This should be extracted from the JWT token
    }
}
