# Implementation Plan: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Branch**: `feature/admin-client-v2-a5` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-admin-client-v2/admin-new-registrations/spec.md`

## Summary

Implement Issue #14: [A5] Danh sách người dùng đăng ký mới (Requirement 1.2.5).
Provide `AdminListUsersHandler` handling `ActionType.ADMIN_LIST_USERS` to query user accounts within `createdAtRange` using `UserRepository.findForAdmin(...)`. Connect `NewRegistrationPanel` in `admin-client` to retrieve and display live user registration data via `SocketClient`.

## Technical Context

**Language/Version**: Java 25 LTS (OpenJDK 25.0.4.1)

**Primary Dependencies**: Spring Boot 3.3.5 (non-web), Spring JDBC (`JdbcTemplate`, `NamedParameterJdbcTemplate`), Jackson 2.17.2, FlatLaf 3.5.2, Swing

**Storage**: PostgreSQL 16+ via Spring JDBC (`users` table)

**Testing**: JUnit 5, AssertJ, Mockito

**Target Platform**: Windows Desktop (Swing) & Java Backend (TCP socket server)

**Project Type**: Multi-module Maven (`common`, `admin-client`, `user-client`, `server`)

**Performance Goals**: < 150 ms query latency for page sizes up to 100 on localhost

**Constraints**: Exactly 4 Maven modules; preserve existing `common` DTOs and database schema; no WebSocket; no `mvn spotless:apply` unless explicitly requested.

## Constitution Check

- [x] **Principle I (Project Scope)**: Follows university requirement 1.2.5 (Issue #14) without adding unauthorized business features.
- [x] **Principle II (Technology Constraints)**: Swing desktop UI, TCP socket networking, Spring JDBC, PostgreSQL.
- [x] **Principle III (Existing Stack)**: Preserves Java 25 LTS, Maven, FlatLaf, Jackson, Spring JDBC.
- [x] **Principle IV (Module Boundaries)**: Exactly 4 modules; `common` unchanged; `admin-client` does not access database; `server` does not access client modules.
- [x] **Principle V (Architecture Responsibilities)**: Backend handles database queries, sorting, pagination, and authorization; Admin client handles presentation.
- [x] **Principle VI (Code Quality)**: Follows existing repository and handler patterns (`AdminGetUserHandler`, `AdminListLoginHistoryHandler`).
- [x] **Principle VII (Testing)**: Unit tests for handler and dispatching.
- [x] **Principle VIII (Development Workflow)**: Active on `feature/admin-client-v2-a5`.
- [x] **Principle IX (AI Usage)**: AI-LOG.md updated for every phase.
- [x] **Principle X (Project Delivery)**: Meets Final Project delivery criteria.

## Project Structure

### Documentation (this feature)

```text
specs/006-admin-client-v2/admin-new-registrations/
├── plan.md              # This file
├── research.md          # Architectural decisions and mappings
├── data-model.md        # Wire DTOs and database mapping
├── quickstart.md        # Verification and quick start guide
├── contracts/           # Protocol envelope definitions
│   └── new-registrations-contracts.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Implementation tasks
```
