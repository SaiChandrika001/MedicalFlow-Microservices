package com.medicalflow.appointmentservice.exception;

public class InvalidAppointmentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidAppointmentException(String message) {
        super(message);
    }

    public InvalidAppointmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
