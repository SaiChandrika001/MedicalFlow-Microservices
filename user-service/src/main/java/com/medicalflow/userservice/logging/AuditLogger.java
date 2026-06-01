package com.medicalflow.userservice.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditLogger {

    public void logLoginAttempt(String email, boolean success, String ipAddress) {
        log.info("AUDIT_EVENT=LOGIN_ATTEMPT, email={}, success={}, ip_address={}, timestamp={}",
                sanitize(email), success, sanitize(ipAddress), System.currentTimeMillis());
    }

    public void logUserRegistration(String email, String ipAddress) {
        log.info("AUDIT_EVENT=USER_REGISTRATION, email={}, ip_address={}, timestamp={}",
                sanitize(email), sanitize(ipAddress), System.currentTimeMillis());
    }

    public void logPasswordChange(String email, String ipAddress) {
        log.info("AUDIT_EVENT=PASSWORD_CHANGE, email={}, ip_address={}, timestamp={}",
                sanitize(email), sanitize(ipAddress), System.currentTimeMillis());
    }

    public void logUnauthorizedAccess(String userId, String resource, String ipAddress) {
        log.warn("AUDIT_EVENT=UNAUTHORIZED_ACCESS, user_id={}, resource={}, ip_address={}, timestamp={}",
                sanitize(userId), sanitize(resource), sanitize(ipAddress), System.currentTimeMillis());
    }

    public void logUserModification(String userId, String modifiedBy, String changes) {
        log.info("AUDIT_EVENT=USER_MODIFICATION, user_id={}, modified_by={}, changes={}, timestamp={}",
                sanitize(userId), sanitize(modifiedBy), sanitize(changes), System.currentTimeMillis());
    }

    private String sanitize(String value) {
        if (value == null) {
            return "NULL";
        }
        return value.replaceAll("['\";\\\\]", "");
    }
}
