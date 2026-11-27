# Implementation Plan: [A9] Biểu đồ số lượng người dùng hoạt động theo tháng trong năm (Issue #16)

**Branch**: `feature/admin-client-v2-a9` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-admin-client-v2/admin-activity-chart/spec.md`

## Summary

Implement Issue #16: [A9] Biểu đồ số lượng người dùng hoạt động theo từng tháng trong năm (Requirement 1.2.9).
Provide `AdminGetMonthlyActiveUsersHandler` handling `ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS` to aggregate distinct active users (app opens) across months 1..12 using `UserActivityLogRepository.countMonthlyActiveUsers(int year)`. Connect `ActivityChartPanel` in `admin-client` to retrieve and render live monthly distinct active user metrics via `SocketClient`, with graceful offline fallback.

## Technical Context

**Language/Version**: Java 25 LTS (OpenJDK 25.0.4.1)

**Primary Dependencies**: Spring Boot 3.3.5 (non-web), Spring JDBC (`NamedParameterJdbcTemplate`), Jackson 2.17.2, FlatLaf 3.5.2, JFreeChart 1.5.5, Swing

**Storage**: PostgreSQL 16+ via Spring JDBC (`user_activity_logs` table)

**Testing**: JUnit 5, AssertJ, Mockito

**Target Platform**: Windows Desktop (Swing) & Java Backend (TCP socket server)

**Project Type**: Multi-module Maven (`common`, `admin-client`, `user-client`, `server`)

**Performance Goals**: < 100 ms query aggregation latency on localhost database

**Constraints**: Exactly 4 Maven modules; preserve existing `common` DTOs (`YearRequest`, `MonthlyCount`); no WebSocket; no `mvn spotless:apply` unless explicitly requested.

## Constitution Check

- [x] **Principle I (Project Scope)**: Follows university requirement 1.2.9 without adding unauthorized business features.
- [x] **Principle II (Technology Constraints)**: Swing desktop UI with JFreeChart, TCP socket networking, Spring JDBC, PostgreSQL.
- [x] **Principle III (Existing Stack)**: Preserves Java 25 LTS, Maven, FlatLaf, Jackson, Spring JDBC, JFreeChart.
- [x] **Principle IV (Module Boundaries)**: Exactly 4 modules; `common` unchanged; `admin-client` does not access database; `server` does not access client modules.
- [x] **Principle V (Architecture Responsibilities)**: Backend handles database aggregation and authorization; Admin client handles presentation.
- [x] **Principle VI (Code Quality)**: Follows existing handler patterns (`AdminGetMonthlyRegistrationsHandler`, `AdminListUsersHandler`).
- [x] **Principle VII (Testing)**: Unit tests for handler and client dispatching.
- [x] **Principle VIII (Development Workflow)**: Active on `feature/admin-client-v2-a9`.
- [x] **Principle IX (AI Usage)**: AI-LOG.md updated for every phase.
- [x] **Principle X (Project Delivery)**: Meets Final Project delivery criteria.

## Project Structure

### Documentation (this feature)

```text
specs/006-admin-client-v2/admin-activity-chart/
├── plan.md              # This file
├── research.md          # Architectural decisions and mappings
├── data-model.md        # Wire DTOs and database mapping
├── contracts/           # Protocol envelope definitions
│   └── activity-chart-contracts.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Implementation tasks
```
