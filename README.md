# HireFlow – Recruitment & Job Application Backend System

<p align="center">
  <img src="https://github.com/avinashee0012/hireflow/actions/workflows/ci.yml/badge.svg" />
</p>

#### Companies struggle to manage job postings, candidate applications, hiring stages, and role-based access in a structured, auditable way. HireFlow provides a backend system to manage job lifecycles, candidate pipelines, and secure role-based access.

## Tracker
- **[Release v0.8.0](https://github.com/avinashee0012/hireflow/pull/30)** - Beta release: Repository Integration Test
- **[Release v0.7.0](https://github.com/avinashee0012/hireflow/pull/25)** - Beta release: CI stabilization & actuator integration
- **[Release v0.6.0](https://github.com/avinashee0012/hireflow/pull/19)** - Beta release: CI pipeline & dockerization
- **[Release v0.5.0](https://github.com/avinashee0012/hireflow/pull/15)** - Beta release: admin module added
- **[Release v0.4.0](https://github.com/avinashee0012/hireflow/pull/13)** - Beta release: unit testing layer added
- **[Release v0.3.0](https://github.com/avinashee0012/hireflow/pull/10)** - Beta release: organisation module and multi-tenancy enforcement
- **[Release v0.2.0](https://github.com/avinashee0012/hireflow/pull/7)** - Beta release: job and application workflow
- **[Release v0.1.0](https://github.com/avinashee0012/hireflow/pull/2)** - Alpha release: authentication and user access

## Current Status (v0.8.0 – Beta)

### Core Features

- JWT-based stateless authentication with RBAC (SUPPORT, ORGADMIN, RECRUITER, CANDIDATE)
- Organisation aggregate with lifecycle management (`ACTIVE / SUSPENDED`)
- Suspension enforcement blocking all state-changing business operations
- Job management (create, update, close, reopen) with ownership validation
- Application workflow (apply, withdraw, shortlist, reject) with strict lifecycle rules
- Role-aware paginated dashboards for jobs and applications
- Multi-tenancy enforcement via Organisation scoping
- System-level Admin module for user governance
- Correlation ID logging and structured error responses
- Actuator endpoints with appropriate security
- Repository integration tests for all JPA repositories

### Architecture

* Clean layered architecture: Controller → Service → Domain → Repository
* Domain-driven entities with guarded state transitions
* Defensive service-layer RBAC and ownership validation
* Intent-based repositories
* Centralized exception handling
* Profile-based configuration (dev, test, prod)
* Environment-driven secrets management

### New in v0.8.0
This PR adds repository integration tests using @DataJpaTest to validate JPA query behavior and database constraints. Tests run against H2 (test profile) and are executed in CI.
  - UserRepo
  - RoleRepo
  - OrganisationRepo
  - JobRepo
  - ApplicationRepo

## Next Milestone

* Integration tests (*in progress*)
* API Documentation (OpenAPI / Swagger)



