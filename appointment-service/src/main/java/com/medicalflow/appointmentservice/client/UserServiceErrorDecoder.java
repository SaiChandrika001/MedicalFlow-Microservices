package com.medicalflow.appointmentservice.client;

import com.medicalflow.appointmentservice.exception.ResourceNotFoundException;
import com.medicalflow.appointmentservice.exception.UserServiceClientException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class UserServiceErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            return new ResourceNotFoundException("User not found when calling user-service: " + methodKey);
        }
        return new UserServiceClientException(
                "Error calling user-service for " + methodKey + ". status=" + response.status());
    }
}
