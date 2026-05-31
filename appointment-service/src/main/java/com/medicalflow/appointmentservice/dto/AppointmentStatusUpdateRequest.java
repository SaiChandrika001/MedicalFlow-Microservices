package com.medicalflow.appointmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateRequest {

    @NotEmpty(message = "Status is required")
    private String status;

    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;
}
