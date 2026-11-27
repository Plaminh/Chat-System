# Implementation Plan: [A2] Admin Login History (Issue #8)

**Branch**: `feature/admin-client-v2` | **Date**: 2026-11-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-admin-client-v2/admin-login-history/spec.md`

## Summary

Implement Issue #8: [A2] Admin Login History (Requirement 1.2.2 and 1.2.1).
Provide `AdminListLoginHistoryHandler` handling `ActionType.ADMIN_LIST_LOGIN_HISTORY` to query chronological login audit records globally or filtered by user using `LoginHistoryRepository.find(...)`. Connect `UserDetailHistoryDialog` in `admin-client` to retrieve and display live login history data via `SocketClient`.

## Technical Context

**Language/Version**: Java 25 LTS (OpenJDK 25.0.4.1)

**Primary Dependencies**: Spring Boot 3.3.5 (non-web), Spring JDBC (`JdbcTemplate`, `NamedParameterJdbcTemplate`), Jackson 2.17.2, FlatLaf 3.5.2, Swing

**Storage**: PostgreSQL 16+ via Spring JDBC (`login_history` table joined with `users`)

**Testing**: JUnit 5, AssertJ, Mockito

**Target Platform**: Windows Desktop (Swing) & Java Backend (TCP socket server)

**Project Type**: Multi-module Maven (`common`, `admin-client`, `user-client`, `server`)

**Performance Goals**: < 150 ms query latency for page sizes up to 100 on localhost

**Constraints**: Exactly 4 Maven modules; preserve existing `common` DTOs and database schema; no WebSocket; no `mvn spotless:apply` unless explicitly requested.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Principle I (Project Scope)**: Follows university requirements 1.2.1 and 1.2.2 without adding unauthorized business features.
- [x] **Principle II (Technology Constraints)**: Swing desktop UI, TCP socket networking, Spring JDBC, PostgreSQL.
- [x] **Principle III (Existing Stack)**: Preserves Java 25 LTS, Maven, FlatLaf, Jackson, Spring JDBC.
- [x] **Principle IV (Module Boundaries)**: Exactly 4 modules; `common` unchanged; `admin-client` does not access database; `server` does not access client modules.
- [x] **Principle V (Architecture Responsibilities)**: Backend handles database queries, sorting, pagination, and authorization; Admin client handles presentation.
- [x] **Principle VI (Code Quality)**: Follows existing repository and handler patterns.
- [x] **Principle VII (Testing)**: Unit and integration tests for handler and dispatching.
- [x] **Principle VIII (Development Workflow)**: Active on `feature/admin-client-v2`.
- [x] **Principle IX (AI Usage)**: AI-LOG.md updated for every phase.
- [x] **Principle X (Project Delivery)**: Meets Final Project delivery criteria.

## Project Structure

### Documentation (this feature)

```text
specs/006-admin-client-v2/admin-login-history/
├── plan.md              # This file
├── research.md          # Phase 0 architectural decisions
├── data-model.md        # Phase 1 wire DTOs and database schema
├── quickstart.md        # Phase 1 verification instructions
├── contracts/           # Phase 1 protocol envelope definitions
│   └── login-history-contracts.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 implementation task breakdown
```

### Source Code

```text
server/src/main/java/com/chat/server/
├── handler/
│   ├── ActionDispatcher.java
│   └── impl/
│       └── AdminListLoginHistoryHandler.java  # New handler for ADMIN_LIST_LOGIN_HISTORY
└── repository/
    └── LoginHistoryRepository.java            # Existing repository method find(...)

server/src/test/java/com/chat/server/
└── handler/
    └── AdminListLoginHistoryHandlerTest.java  # Handler tests

admin-client/src/main/java/com/chat/admin/
├── view/
│   ├── UserDetailHistoryDialog.java           # Connect to SocketClient
│   └── UserManagementPanel.java
└── network/
    └── SocketClient.java
```
