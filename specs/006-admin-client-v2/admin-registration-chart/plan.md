# Implementation Plan: [A6] Biểu đồ số lượng người dùng đăng ký mới (Issue #15)

**Branch**: `feature/admin-client-v2-a6` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-admin-client-v2/admin-registration-chart/spec.md`

## Summary

Implement Issue #15: [A6] Biểu đồ số lượng người dùng đăng ký mới theo 12 tháng (Requirement 1.2.6).
Provide `AdminGetMonthlyRegistrationsHandler` handling `ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS` to aggregate user registrations across months 1..12 using `UserRepository.countMonthlyRegistrations(int year)`. Connect `RegistrationChartPanel` in `admin-client` to retrieve and render live monthly registration data via `SocketClient`, with graceful offline fallback.

## Technical Context

**Language/Version**: Java 25 LTS (OpenJDK 25.0.4.1)

**Primary Dependencies**: Spring Boot 3.3.5 (non-web), Spring JDBC (`NamedParameterJdbcTemplate`), Jackson 2.17.2, FlatLaf 3.5.2, JFreeChart 1.5.5, Swing

**Storage**: PostgreSQL 16+ via Spring JDBC (`users` table)

**Testing**: JUnit 5, AssertJ, Mockito

**Target Platform**: Windows Desktop (Swing) & Java Backend (TCP socket server)

**Project Type**: Multi-module Maven (`common`, `admin-client`, `user-client`, `server`)

**Performance Goals**: < 100 ms query aggregation latency on localhost database

**Constraints**: Exactly 4 Maven modules; preserve existing `common` DTOs (`YearRequest`, `MonthlyCount`); no WebSocket; no `mvn spotless:apply` unless explicitly requested.

## Constitution Check

- [x] **Principle I (Project Scope)**: Follows university requirement 1.2.6 without adding unauthorized business features.
- [x] **Principle II (Technology Constraints)**: Swing desktop UI with JFreeChart, TCP socket networking, Spring JDBC, PostgreSQL.
- [x] **Principle III (Existing Stack)**: Preserves Java 25 LTS, Maven, FlatLaf, Jackson, Spring JDBC, JFreeChart.
- [x] **Principle IV (Module Boundaries)**: Exactly 4 modules; `common` unchanged; `admin-client` does not access database; `server` does not access client modules.
- [x] **Principle V (Architecture Responsibilities)**: Backend handles database aggregation and authorization; Admin client handles presentation.
- [x] **Principle VI (Code Quality)**: Follows existing handler patterns (`AdminListUsersHandler`, `AdminListLoginHistoryHandler`).
- [x] **Principle VII (Testing)**: Unit tests for handler and client dispatching.
- [x] **Principle VIII (Development Workflow)**: Active on `feature/admin-client-v2-a6`.
- [x] **Principle IX (AI Usage)**: AI-LOG.md updated for every phase.
- [x] **Principle X (Project Delivery)**: Meets Final Project delivery criteria.

## Project Structure

### Documentation (this feature)

```text
specs/006-admin-client-v2/admin-registration-chart/
├── plan.md              # This file
├── research.md          # Architectural decisions and mappings
├── data-model.md        # Wire DTOs and database mapping
├── contracts/           # Protocol envelope definitions
│   └── registration-chart-contracts.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Implementation tasks
```
