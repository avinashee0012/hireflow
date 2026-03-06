# HireFlow – Recruitment & Job Application Backend System

<p align="center">
  <img src="https://github.com/avinashee0012/hireflow/actions/workflows/ci.yml/badge.svg" />
</p>

HireFlow is a recruitment backend system built with Spring Boot. It manages job postings, candidate applications, hiring pipelines, and role-based access across multiple organisations. The project demonstrates layered architecture, RBAC security, multi-tenancy enforcement, lifecycle-driven domain modeling, and a complete testing pyramid.

---

# Tracker

- **[Release v1.0.0](https://github.com/avinashee0012/hireflow/pull/36)** – Stable release

---

# Current Status (v1.0.0 – Stable)

HireFlow is now a stable backend system demonstrating:

- Multi-tenant architecture
- Role-based access control
- Domain-driven lifecycle modeling
- Layered backend architecture
- Full backend testing pyramid
- CI pipeline and containerized runtime

---

# Core Features

### Authentication & Security
- JWT-based stateless authentication
- Hierarchical RBAC roles:
  - SUPPORT
  - ORGADMIN
  - RECRUITER
  - CANDIDATE
- Method-level authorization using Spring Security
- Defensive service-layer access validation

### Organisation Management
- Multi-tenant architecture using organisation boundaries
- Organisation lifecycle:
  - `ACTIVE`
  - `SUSPENDED`
- Suspension enforcement preventing business operations

### Job Management
- Job creation and updates
- Recruiter ownership enforcement
- Job lifecycle:
  - OPEN
  - CLOSED
- Paginated recruiter dashboards

### Application Workflow
Candidate application lifecycle:

- APPLY
- WITHDRAW
- SHORTLIST
- REJECT

Strict lifecycle transition rules are enforced at the domain layer.

### Admin Governance
System administrators can:

- View users across organisations
- Activate / deactivate accounts
- Update roles
- Enforce hierarchy rules
- Prevent self-administrative actions

### Observability
- Spring Boot Actuator endpoints
- Health monitoring
- Structured logging with correlation IDs
- Secure actuator access

---

# Architecture

HireFlow follows a clean layered architecture:

```

Controller → Service → Domain → Repository

```

### Controller Layer
- REST endpoints
- Request validation
- Method-level RBAC using `@PreAuthorize`

### Service Layer
- Business logic
- Defensive RBAC checks
- Multi-tenant validation
- Transaction management

### Domain Layer
- Entity lifecycle rules
- Guarded state transitions
- Business invariants

### Repository Layer
- Intent-based Spring Data repositories
- Organisation-scoped queries
- Pagination support

---

# Security Model

The system uses JWT-based stateless authentication.

Security enforcement happens at two levels:

1. **Controller Layer**
   - Role-based endpoint access via `@PreAuthorize`

2. **Service Layer**
   - Ownership validation
   - Organisation boundary checks
   - Defensive RBAC enforcement

This layered approach prevents privilege escalation and cross-tenant access.

---

# Multi-Tenancy Design

HireFlow uses organisation-based multi-tenancy.

Key rules:

- Every business entity belongs to an `organisationId`
- Service layer validates organisation ownership
- Repository queries enforce organisation scoping
- Cross-organisation access is prevented

This design ensures strict tenant isolation.

---

# Testing Strategy

**Domain Tests:** *Validate entity lifecycle rules and business invariants.*

**Service Tests:** *Verify RBAC enforcement, ownership checks, and business logic.*

**Controller Slice Tests:** *Validate HTTP behavior, security configuration, and request validation.*

**Repository Integration Tests:** *Ensure correct JPA queries, pagination behavior, and database constraints.*

All tests run automatically in CI.

---

# Infrastructure & Tooling

- **Java 21**
- **Spring Boot 3.4**
- **Spring Security (JWT + Method Security)**
- **Spring Data JPA / Hibernate**
- **MySQL (dev / production)**
- **H2 (test environment)**
- **GitHub Actions CI pipeline**
- **Docker multi-stage containerization**
- **Spring Boot Actuator**

---

# Running the Project

## Local Development

```

mvn spring-boot:run

```

The application will start on:

```

[http://localhost:8080](http://localhost:8080)

```

---

## Running Tests

```

mvn clean verify

```

Tests run using the `test` profile with an in-memory H2 database.

---

## Docker Build

Build the container image:

```

docker build -t hireflow .

```

Run the container:

```

docker run -p 8080:8080 hireflow

```

---

# API Endpoints

| Method | Endpoint                                     | Description                                    |
| ------ | -------------------------------------------- | ---------------------------------------------- |
| POST   | `/api/auth/register`                         | Register a new user                            |
| POST   | `/api/auth/login`                            | Authenticate user and return JWT token         |
| POST   | `/api/organisations`                         | Create a new organisation                      |
| GET    | `/api/organisations/{orgId}`                 | Get organisation details                       |
| PATCH  | `/api/organisations/{orgId}/activate`        | Activate an organisation                       |
| PATCH  | `/api/organisations/{orgId}/suspend`         | Suspend an organisation                        |
| POST   | `/api/jobs`                                  | Create a new job                               |
| GET    | `/api/jobs`                                  | Get paginated list of jobs                     |
| GET    | `/api/jobs/{jobId}`                          | Get job details                                |
| PUT    | `/api/jobs/{jobId}`                          | Update job details                             |
| PATCH  | `/api/jobs/{jobId}/close`                    | Close a job                                    |
| PATCH  | `/api/jobs/{jobId}/reopen`                   | Reopen a job                                   |
| POST   | `/api/applications/{jobId}`                  | Apply to a job                                 |
| GET    | `/api/applications`                          | Get paginated list of applications             |
| PATCH  | `/api/applications/{applicationId}/withdraw` | Withdraw a job application                     |
| PATCH  | `/api/applications/{applicationId}/status`   | Update application status (shortlist / reject) |
| GET    | `/api/admin/users`                           | Get paginated list of users                    |
| PATCH  | `/api/admin/users/{id}/activate`             | Activate a user                                |
| PATCH  | `/api/admin/users/{id}/deactivate`           | Deactivate a user                              |
| PATCH  | `/api/admin/users/{id}/roles`                | Update user roles                              |
---

# License

This project is open source and available under the MIT License.
