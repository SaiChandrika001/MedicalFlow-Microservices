# Structured JSON Logging Sample

These sample logs demonstrate the structured JSON format, MDC correlation fields, request tracking, and log levels.

```json
{ "timestamp": "2026-05-30T12:27:45.123Z", "app": "appointment-service", "logLevel": "INFO", "loggerName": "com.medicalflow.appointmentservice.controller.AppointmentController", "threadName": "http-nio-8082-exec-1", "message": "Received appointment create request", "correlationId": "a6fcb2f6-3f29-4b8d-9d80-b1f3125f8d3a", "requestId": "e9a6c8a2-51be-48f2-96df-9dcdcd8f2a1a", "httpMethod": "POST", "requestPath": "/api/appointments", "queryString": "", "clientIp": "127.0.0.1" }
```

```json
{ "timestamp": "2026-05-30T12:27:45.129Z", "app": "user-service", "logLevel": "DEBUG", "loggerName": "com.medicalflow.userservice.service.UserService", "threadName": "http-nio-8081-exec-2", "message": "Loading user by email", "correlationId": "a6fcb2f6-3f29-4b8d-9d80-b1f3125f8d3a", "requestId": "e9a6c8a2-51be-48f2-96df-9dcdcd8f2a1a", "httpMethod": "GET", "requestPath": "/api/users/123", "queryString": "", "clientIp": "127.0.0.1" }
```

```json
{ "timestamp": "2026-05-30T12:27:45.145Z", "app": "api-gateway", "logLevel": "WARN", "loggerName": "org.springframework.cloud.gateway.filter.GatewayMetricsFilter", "threadName": "reactor-http-nio-1", "message": "Request took longer than expected", "correlationId": "a6fcb2f6-3f29-4b8d-9d80-b1f3125f8d3a", "requestId": "e9a6c8a2-51be-48f2-96df-9dcdcd8f2a1a", "httpMethod": "GET", "requestPath": "/api/users/123", "queryString": "" }
```

```json
{ "timestamp": "2026-05-30T12:27:45.150Z", "app": "report-service", "logLevel": "ERROR", "loggerName": "com.medicalflow.reportservice.service.ReportServiceImpl", "threadName": "http-nio-8083-exec-4", "message": "Failed to upload report to S3", "correlationId": "a6fcb2f6-3f29-4b8d-9d80-b1f3125f8d3a", "requestId": "e9a6c8a2-51be-48f2-96df-9dcdcd8f2a1a", "httpMethod": "POST", "requestPath": "/api/reports/upload", "queryString": "", "clientIp": "127.0.0.1" }
```
