# HireFlow – Recruitment & Job Application Backend System

<p align="center">
  <img src="https://github.com/avinashee0012/hireflow/actions/workflows/ci.yml/badge.svg" />
</p>

#### Companies struggle to manage job postings, candidate applications, hiring stages, and role-based access in a structured, auditable way. HireFlow provides a backend system to manage job lifecycles, candidate pipelines, and secure role-based access.

---

# Tracker
- **[Release v0.9.0](https://github.com/avinashee0012/hireflow/pull/34)** - Beta release: project refinement/review
- **[Release v0.8.0](https://github.com/avinashee0012/hireflow/pull/30)** - Beta release: repository integration tests
- **[Release v0.7.0](https://github.com/avinashee0012/hireflow/pull/25)** - Beta release: CI stabilization & actuator integration
- **[Release v0.6.0](https://github.com/avinashee0012/hireflow/pull/19)** - Beta release: CI pipeline & dockerization
- **[Release v0.5.0](https://github.com/avinashee0012/hireflow/pull/15)** - Beta release: admin module added
- **[Release v0.4.0](https://github.com/avinashee0012/hireflow/pull/13)** - Beta release: unit testing layer added
- **[Release v0.3.0](https://github.com/avinashee0012/hireflow/pull/10)** - Beta release: organisation module and multi-tenancy enforcement
- **[Release v0.2.0](https://github.com/avinashee0012/hireflow/pull/7)** - Beta release: job and application workflow
- **[Release v0.1.0](https://github.com/avinashee0012/hireflow/pull/2)** - Alpha release: authentication and user access

---

# Current Status (v0.9.0 – Beta)

HireFlow is a **production-style backend system** demonstrating multi-tenancy, role-based access control, lifecycle-driven domain modeling, and disciplined backend engineering practices.

---

# Core Features

- JWT-based stateless authentication
- Role-based access control (SUPPORT, ORGADMIN, RECRUITER, CANDIDATE)
- Organisation lifecycle management (`ACTIVE / SUSPENDED`)
- Suspension enforcement blocking all state-changing business operations
- Job lifecycle management (create, update, close, reopen)
- Candidate application workflow (apply, withdraw, shortlist, reject)
- Role-aware paginated dashboards for jobs and applications
- Multi-tenancy enforcement via organisation scoping
- System-level Admin module for user governance
- Structured logging with correlation ID tracing
- Consistent API error responses
- Spring Boot Actuator for health and system insights
- Repository integration tests for all JPA repositories

---

# Architecture

The system follows a **clean layered architecture**:
`Controller → Service → Domain → Repository`


### Design Principles

- Domain-driven entities with guarded lifecycle transitions
- Defensive service-layer RBAC validation
- Organisation-based multi-tenancy enforcement
- Intent-based repository queries
- Centralized exception handling
- Profile-based configuration (`dev`, `test`, `prod`)
- Environment-driven configuration and secrets

---

# Testing Strategy

HireFlow follows a layered testing approach:

- **Domain Tests**  
  Validate entity lifecycle rules and domain invariants.

- **Service Tests**  
  Verify RBAC enforcement, ownership checks, and business logic.

- **Controller Slice Tests**  
  Validate API contracts, HTTP status codes, and security configuration.

- **Repository Integration Tests**  
  Ensure correct JPA query behavior, pagination logic, and database integrity constraints.

All tests run in CI using the **test profile with H2**.

---

# Infrastructure & Tooling

- **Java 21**
- **Spring Boot 3.4**
- **Spring Security (JWT + Method Security)**
- **Spring Data JPA / Hibernate**
- **MySQL (dev/prod)**
- **H2 (test environment)**
- **GitHub Actions CI Pipeline**
- **Docker multi-stage build**
- **Spring Boot Actuator**

---

# New in v0.9.0

This release focuses on **refinement and stabilization** before the v1.0 milestone.

No new business features were introduced.

### Improvements

- Repository layer review and query cleanup
- Logging improvements and correlation tracing
- API response consistency improvements
- Global exception handling refinement
- Codebase consistency improvements
- Endpoint review and REST convention alignment
- Documentation updates and architecture clarification

The goal of this release is to improve **maintainability, clarity, and production readiness**.

---

# Next Milestone

### **v1.0.0 – First Major Release**

The upcoming release will mark the project as **production-ready portfolio work**, including:

- Final stability pass
- Documentation polish
- Clean release tagging
- Finalized architecture documentation