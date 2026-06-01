package com.medicalflow.userservice.event;

import java.time.Instant;

public class UserRegisteredEvent {
    private String email;
    private String fullName;
    private String role;
    private Instant timestamp;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(String email, String fullName, String role, Instant timestamp) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.timestamp = timestamp;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
