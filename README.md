# HireFlow – Recruitment & Job Application Backend System

#### Companies struggle to manage job postings, candidate applications, hiring stages, and role-based access in a structured, auditable way. HireFlow provides a backend system to manage job lifecycles, candidate pipelines, and secure role-based access.

## Tracker
- **[Release v0.4.0](https://github.com/avinashee0012/hireflow/pull/13)** - Beta release: unit testing layer added
- **[Release v0.3.0](https://github.com/avinashee0012/hireflow/pull/10)** - Beta release: organisation module and multi-tenancy enforcement
- **[Release v0.2.0](https://github.com/avinashee0012/hireflow/pull/7)** - Beta release: job and application workflow
- **[Release v0.1.0](https://github.com/avinashee0012/hireflow/pull/2)** - Alpha release: authentication and user access

## Current Status (v0.4.0 – Beta)

### Core Features

* JWT-based stateless authentication with RBAC (SUPPORT, ORGADMIN, RECRUITER, CANDIDATE)
* Organisation aggregate with lifecycle management (`ACTIVE / SUSPENDED`)
* Suspension enforcement blocking all state-changing business operations
* Job management (create, update, close, reopen) with ownership validation
* Application workflow (apply, withdraw, shortlist, reject) with strict lifecycle rules
* Role-aware paginated dashboards for jobs and applications
* Multi-tenancy enforcement via Organisation scoping
* Correlation ID logging and structured error responses

### Architecture

* Clean layered architecture: Controller → Service → Domain → Repository
* Domain-driven entities with guarded state transitions
* Defensive service-layer RBAC and ownership validation
* Intent-based repositories
* Centralized exception handling

### Testing Coverage (New in v0.4.0)

* Domain unit tests validating business invariants and state transitions
* Service unit tests covering RBAC branches, multi-tenancy enforcement, duplicate prevention, and illegal transitions
* Controller slice tests validating:
  * HTTP status contracts
  * Security behavior
  * Validation constraints
  * JSON response contracts

## Next Milestone

* Integration tests (*deferred*)
* CI/CD pipeline with GitHub Actions (*planned next*)



