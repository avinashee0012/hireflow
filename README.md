# HireFlow – Recruitment & Job Application Backend System

#### Companies struggle to manage job postings, candidate applications, hiring stages, and role-based access in a structured, auditable way. HireFlow provides a backend system to manage job lifecycles, candidate pipelines, and secure role-based access.

## Tracker
- **[Release v0.3.0](https://github.com/avinashee0012/hireflow/pull/10)** - Beta release: organisation module and multi-tenancy enforcement
- **[Release v0.2.0](https://github.com/avinashee0012/hireflow/pull/7)** - Beta release: job and application workflow
- **[Release v0.1.0](https://github.com/avinashee0012/hireflow/pull/2)** - Alpha release: authentication and user access

## Current Status (v0.3.0 – Beta)

The system now supports a complete end-to-end recruitment workflow:

- JWT-based stateless authentication with RBAC (SUPPORT, ORGADMIN, RECRUITER, CANDIDATE)
- Organisation aggregate with lifecycle (`ACTIVE / SUSPENDED`)
- Suspension enforcement blocking all state-changing business operations
- Job management (create, update, close, reopen) with ownership validation
- Application workflow (apply, withdraw, shortlist, reject) with strict lifecycle rules
- Role-aware paginated dashboards for jobs and applications
- Clean layered architecture with intent-based repositories and centralized exception handling


