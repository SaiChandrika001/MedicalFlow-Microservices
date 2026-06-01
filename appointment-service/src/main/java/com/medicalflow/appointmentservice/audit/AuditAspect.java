package com.medicalflow.appointmentservice.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private final AuditRepository repository;
    private final HttpServletRequest request;

    public AuditAspect(AuditRepository repository, HttpServletRequest request) {
        this.repository = repository;
        this.request = request;
    }

    @Around("@annotation(audit)")
    public Object aroundAudited(ProceedingJoinPoint pjp, Audit audit) throws Throwable {
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(audit.action());
        log.setServiceName("appointment-service");
        log.setRequestPath(request.getRequestURI());

        try {
            Object res = pjp.proceed();
            log.setStatus(HttpStatus.OK.value());
            repository.save(log);
            return res;
        } catch (Throwable t) {
            log.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            repository.save(log);
            throw t;
        }
    }
}
