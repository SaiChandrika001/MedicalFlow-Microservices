# MedicalFlow-Microservices Architecture

Enterprise-grade architecture documentation for **MedicalFlow-Microservices**, a healthcare management platform built with Java 17, Spring Boot 3, Spring Cloud Gateway, MySQL, Kafka, Redis, Docker, Prometheus, Grafana, Jaeger, OpenTelemetry, and GitHub Actions.

## System Architecture

```mermaid
flowchart TB
    %% External Layer
    subgraph EXT["External Access Layer"]
        CLIENT["Client Applications<br/>Web UI | Mobile | REST Client"]
        DEV["Developer / Maintainer"]
    end

    %% CICD Layer
    subgraph CICD["CI/CD & Quality Gate Layer"]
        GHA["GitHub Actions<br/>CI/CD Pipeline"]
        MAVEN["Maven Build & Test"]
        DOCKERVAL["Docker Build Validation"]
        OWASP["OWASP Dependency Check"]
    end

    %% Edge and Security Layer
    subgraph EDGE["Edge, Security & Traffic Management Layer"]
        GW["API Gateway :8080<br/>Spring Cloud Gateway<br/>Routing | Rate Limiting | CORS"]
        SECURITY["Security Controls<br/>JWT Authentication<br/>Refresh Tokens<br/>RBAC<br/>Gateway Security Headers"]
        REDIS["Redis<br/>Rate Limiting | Cache"]
    end

    %% Application Layer
    subgraph APP["Microservices Application Layer"]
        USER["User Service :8081<br/>Auth | Users | Profiles<br/>JWT + Refresh Tokens"]
        APPT["Appointment Service :8082<br/>Scheduling | Status Flow<br/>Audit | Pagination"]
        REPORT["Report Service :8084<br/>Medical Reports<br/>Upload | Download | Metadata"]
        NOTIFY["Notification Service :8085<br/>Kafka Consumers<br/>Email Notifications"]
    end

    %% Data Layer
    subgraph DATA["Database & Migration Layer"]
        UDB[("MySQL Users DB<br/>medicalflow_users")]
        ADB[("MySQL Appointments DB<br/>medicalflow_appointments")]
        RDB[("MySQL Reports DB<br/>medicalflow_reports")]
        FLYWAY["Flyway<br/>Schema Versioning"]
    end

    %% Messaging Layer
    subgraph MSG["Event Streaming Layer"]
        KAFKA["Apache Kafka"]
        T1["Topic: user-registered"]
        T2["Topic: appointment-booked"]
        T3["Topic: appointment-cancelled"]
        T4["Topic: report-uploaded"]
    end

    %% Observability Layer
    subgraph OBS["Monitoring & Observability Layer"]
        OTEL["OpenTelemetry Collector"]
        PROM["Prometheus<br/>Metrics Scraping"]
        GRAF["Grafana<br/>Dashboards"]
        JAEGER["Jaeger<br/>Distributed Tracing"]
        ACT["Spring Boot Actuator<br/>/actuator/prometheus"]
    end

    %% Deployment Layer
    subgraph DEPLOY["Container Runtime Layer"]
        DOCKER["Docker Images"]
        COMPOSE["Docker Compose<br/>Local Orchestration"]
    end

    CLIENT -->|"HTTPS / REST<br/>/api/v1/**"| GW
    GW -->|"JWT validation<br/>Route rewrite"| SECURITY
    SECURITY --> GW
    GW <-->|"Rate-limit tokens"| REDIS
    GW -->|"REST /auth /users"| USER
    GW -->|"REST /appointments"| APPT
    GW -->|"REST /reports"| REPORT

    USER -->|"JPA"| UDB
    APPT -->|"JPA"| ADB
    REPORT -->|"JPA"| RDB
    FLYWAY -.->|"Versioned migrations"| UDB
    FLYWAY -.->|"Versioned migrations"| ADB
    FLYWAY -.->|"Versioned migrations"| RDB

    APPT -->|"Feign / REST user lookup"| USER
    APPT <-->|"Cache appointments"| REDIS

    USER -->|"Publish event"| T1
    APPT -->|"Publish event"| T2
    APPT -->|"Publish event"| T3
    REPORT -->|"Publish event"| T4
    T1 --> KAFKA
    T2 --> KAFKA
    T3 --> KAFKA
    T4 --> KAFKA
    KAFKA -->|"Consume events"| NOTIFY
    NOTIFY -->|"SMTP email delivery"| CLIENT

    USER --> ACT
    APPT --> ACT
    REPORT --> ACT
    GW --> ACT
    ACT -->|"Metrics"| PROM
    USER -->|"Traces"| OTEL
    APPT -->|"Traces"| OTEL
    REPORT -->|"Traces"| OTEL
    GW -->|"Traces"| OTEL
    OTEL --> JAEGER
    OTEL --> PROM
    PROM --> GRAF

    DEV --> GHA
    GHA --> MAVEN
    GHA --> DOCKERVAL
    GHA --> OWASP
    MAVEN --> DOCKER
    DOCKERVAL --> DOCKER
    DOCKER --> COMPOSE
    COMPOSE --> GW
    COMPOSE --> USER
    COMPOSE --> APPT
    COMPOSE --> REPORT
    COMPOSE --> NOTIFY
```

## Enterprise Architecture View

| Layer | Components | Responsibility |
| --- | --- | --- |
| External Access | Client Applications, REST Clients | Initiate authenticated healthcare workflows |
| Edge & Security | API Gateway, JWT, Refresh Tokens, RBAC, Redis Rate Limiter | Centralized routing, authentication, authorization, traffic control |
| Application | User, Appointment, Report, Notification Services | Business capabilities with independent ownership |
| Data | MySQL Users DB, Appointments DB, Reports DB, Flyway | Domain-owned persistence and database migration governance |
| Messaging | Kafka topics | Asynchronous event propagation and service decoupling |
| Observability | Actuator, OpenTelemetry, Prometheus, Grafana, Jaeger | Health, metrics, distributed tracing, operational dashboards |
| CI/CD | GitHub Actions, Maven, Docker validation, OWASP Dependency Check | Automated build, test, security scanning, and deployment readiness |
| Runtime | Docker, Docker Compose | Local and containerized service orchestration |

## Kafka Event Flow

```mermaid
flowchart LR
    USER["User Service"] -->|"UserRegisteredEvent"| UR["user-registered"]
    APPT["Appointment Service"] -->|"AppointmentBookedEvent"| AB["appointment-booked"]
    APPT -->|"AppointmentCancelledEvent"| AC["appointment-cancelled"]
    REPORT["Report Service"] -->|"ReportUploadedEvent"| RU["report-uploaded"]

    UR --> KAFKA["Apache Kafka"]
    AB --> KAFKA
    AC --> KAFKA
    RU --> KAFKA

    KAFKA -->|"@KafkaListener"| NOTIFY["Notification Service"]
    NOTIFY -->|"Welcome Email"| EMAIL1["Registered User"]
    NOTIFY -->|"Confirmation Email"| EMAIL2["Patient / Doctor"]
    NOTIFY -->|"Cancellation Email"| EMAIL3["Patient / Doctor"]
    NOTIFY -->|"Report Available Email"| EMAIL4["Patient"]
```

## Observability Flow

```mermaid
flowchart LR
    GW["API Gateway"]
    USER["User Service"]
    APPT["Appointment Service"]
    REPORT["Report Service"]

    GW -->|"Actuator metrics"| PROM["Prometheus"]
    USER -->|"Actuator metrics"| PROM
    APPT -->|"Actuator metrics"| PROM
    REPORT -->|"Actuator metrics"| PROM

    GW -->|"OTLP traces"| OTEL["OpenTelemetry Collector"]
    USER -->|"OTLP traces"| OTEL
    APPT -->|"OTLP traces"| OTEL
    REPORT -->|"OTLP traces"| OTEL

    OTEL --> JAEGER["Jaeger"]
    PROM --> GRAFANA["Grafana"]
```

## CI/CD Pipeline

```mermaid
flowchart LR
    PUSH["Push / Pull Request"] --> GHA["GitHub Actions"]
    GHA --> SETUP["Set up JDK 17"]
    SETUP --> BUILD["Maven Build"]
    BUILD --> TEST["Maven Tests"]
    TEST --> FLYWAY["Flyway Validation"]
    TEST --> DOCKER["Docker Compose + Image Build Validation"]
    TEST --> OWASP["OWASP Dependency Check"]
    TEST --> PMD["PMD Code Quality"]
    FLYWAY --> SUMMARY["Pipeline Summary"]
    DOCKER --> SUMMARY
    OWASP --> SUMMARY
    PMD --> SUMMARY
```

## Login Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway :8080
    participant Security as Gateway Security
    participant UserSvc as User Service :8081
    participant UserDB as MySQL Users DB
    participant Kafka as Kafka
    participant Notify as Notification Service

    Client->>Gateway: POST /api/v1/auth/login
    Gateway->>Security: Apply route, CORS, security headers
    Security-->>Gateway: Public auth route allowed
    Gateway->>UserSvc: POST /auth/login
    UserSvc->>UserDB: Find user by email
    UserDB-->>UserSvc: User credentials + role
    UserSvc->>UserSvc: Verify password
    UserSvc->>UserSvc: Generate JWT access token
    UserSvc->>UserDB: Create refresh token
    UserDB-->>UserSvc: Persisted refresh token
    UserSvc-->>Gateway: AuthResponse(accessToken, refreshToken)
    Gateway-->>Client: 200 OK with tokens

    Note over UserSvc,Kafka: Registration flow publishes user-registered event after signup
    UserSvc-->>Kafka: user-registered event
    Kafka-->>Notify: Consume event
```

## Appointment Booking Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Patient
    participant Gateway as API Gateway :8080
    participant Security as JWT / RBAC
    participant ApptSvc as Appointment Service :8082
    participant UserSvc as User Service :8081
    participant Redis as Redis
    participant ApptDB as MySQL Appointments DB
    participant Kafka as Kafka
    participant Notify as Notification Service

    Patient->>Gateway: POST /api/v1/appointments<br/>Authorization: Bearer token
    Gateway->>Security: Validate JWT and route policy
    Security-->>Gateway: Authenticated principal + role
    Gateway->>ApptSvc: POST /appointments
    ApptSvc->>Security: Enforce @PreAuthorize role rules
    ApptSvc->>UserSvc: Validate patient / doctor context
    UserSvc-->>ApptSvc: User profile response
    ApptSvc->>ApptDB: Save appointment
    ApptDB-->>ApptSvc: Appointment persisted
    ApptSvc->>Redis: Update / invalidate appointment cache
    ApptSvc->>Kafka: Publish appointment-booked
    Kafka-->>Notify: Consume appointment-booked
    Notify-->>Patient: Send appointment confirmation email
    ApptSvc-->>Gateway: 201 Created + appointment response
    Gateway-->>Patient: 201 Created
```

## Report Upload Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Doctor
    participant Gateway as API Gateway :8080
    participant Security as JWT / RBAC
    participant ReportSvc as Report Service :8084
    participant ReportDB as MySQL Reports DB
    participant Storage as File Storage / S3
    participant Kafka as Kafka
    participant Notify as Notification Service
    actor Patient

    Doctor->>Gateway: POST /api/v1/reports/upload<br/>multipart/form-data + Bearer token
    Gateway->>Security: Validate JWT, RBAC, route policy
    Security-->>Gateway: Doctor/Admin authorized
    Gateway->>ReportSvc: POST /reports/upload
    ReportSvc->>Security: Enforce @PreAuthorize(DOCTOR, ADMIN)
    ReportSvc->>Storage: Store report file
    Storage-->>ReportSvc: File path or object key
    ReportSvc->>ReportDB: Save report metadata
    ReportDB-->>ReportSvc: Report persisted
    ReportSvc->>Kafka: Publish report-uploaded
    Kafka-->>Notify: Consume report-uploaded
    Notify-->>Patient: Send report available email
    ReportSvc-->>Gateway: 200 OK + report metadata
    Gateway-->>Doctor: 200 OK
```

## Draw.io Compatible Architecture

The editable Draw.io source is available at:

```text
docs/diagrams/medicalflow-enterprise-architecture.drawio
```

Open it with [draw.io / diagrams.net](https://app.diagrams.net/) using **File -> Open From -> Device**.

## PNG-Style Layout Description

Use this layout when exporting to PNG for README, portfolio, or interview presentation decks:

- Canvas: 16:9 landscape, recommended size `1920x1080`.
- Style: enterprise architecture, clean white background, subtle grouped containers, consistent spacing, rounded rectangles, minimal shadows.
- Top row: External clients on the left, GitHub Actions CI/CD lane on the right.
- Center top: API Gateway as the main ingress node, with a security control band directly attached.
- Center row: four microservices arranged left to right: User Service, Appointment Service, Report Service, Notification Service.
- Data row: MySQL Users DB under User Service, MySQL Appointments DB under Appointment Service, MySQL Reports DB under Report Service; Flyway shown as a governance component connected to all three databases.
- Messaging row: Kafka centered between producer services and Notification Service; topics shown as individual topic boxes.
- Cache: Redis near API Gateway and Appointment Service, with arrows for rate limiting and cache access.
- Observability band: Prometheus, Grafana, OpenTelemetry Collector, and Jaeger in a bottom horizontal lane connected from Gateway and services.
- Arrows:
  - Solid blue arrows for synchronous REST calls.
  - Solid orange arrows for Kafka event publishing and consumption.
  - Dashed green arrows for metrics and tracing.
  - Purple arrows for CI/CD build and deployment readiness flow.
- Footer label: `MedicalFlow-Microservices | Java 17 | Spring Boot 3 | Kafka | MySQL | Observability | CI/CD`.

Suggested README image reference after PNG export:

```md
![MedicalFlow Enterprise Architecture](docs/assets/medicalflow-enterprise-architecture.png)
```
