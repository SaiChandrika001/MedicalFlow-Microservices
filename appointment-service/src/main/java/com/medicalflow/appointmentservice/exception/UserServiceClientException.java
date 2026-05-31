package com.medicalflow.appointmentservice.exception;

public class UserServiceClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserServiceClientException(String message) {
        super(message);
    }

    public UserServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
