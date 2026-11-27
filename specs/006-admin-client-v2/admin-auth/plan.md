# Implementation Plan: [A0] Admin Authentication & Authorization (Issue #7)

**Branch**: `feature/admin-client-v2` | **Date**: 2026-11-23 | **Spec**: [spec.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/spec.md)

**Input**: Feature specification from `specs/006-admin-client-v2/admin-auth/spec.md`

## Summary

Implement robust authentication and administrator authorization gating for the Admin Desktop Client (`admin-client`) communicating over TCP sockets with the backend (`server`). The workflow implements:
1. `LOGIN` via `LoginHandler` using BCrypt password verification, checking `AccountStatus` (`ACTIVE`, `LOCKED`, `DELETED`), and recording login history.
2. Two-step admin role verification: Client calls `ADMIN_GET_USER` with its own `userId` immediately after `LOGIN`. Server `ActionDispatcher` enforces `ADMIN` role. If unauthorized, client dispatches `LOGOUT` and displays *"Tài khoản không có quyền quản trị"*. If admin, client opens `AdminMainFrame`.
3. `LOGOUT` via `LogoutHandler` clearing `SessionContext`, recording `logged_out_at` in `login_history`, and unbinding from `OnlineSessionRegistry`.

## Technical Context

**Language/Version**: Java 25 LTS (compatible with project `--enable-preview` and toolchain)
**Primary Dependencies**: FlatLaf 3.5.4, Spring Boot 3.4.1 (non-web mode), Jackson 2.18.2, jBCrypt 0.4, Spring JDBC / PostgreSQL Driver
**Storage**: PostgreSQL (via Spring `NamedParameterJdbcTemplate`, tables `users`, `login_history`)
**Testing**: JUnit 5, AssertJ, Mockito
**Target Platform**: Desktop (Windows/Linux/macOS) Java Swing + Backend Socket Server
**Project Type**: Multi-module Maven (`common`, `admin-client`, `user-client`, `server`)
**Performance Goals**: Localhost login and role verification completes in under 200ms.
**Constraints**: Exactly 4 Maven modules (no `client-common`), no changes to `common` DTOs, no WebSockets, no REST client protocol.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Principle I: Academic Integrity & Non-Expansion**: Conforms to Issue #7 [A0] without inventing new business features or changing protocol schema.
- [x] **Principle II: Four-Module Rule**: Only touches `server` and `admin-client`. `common` remains strictly untouched. No new modules created.
- [x] **Principle III: Technology Stack Preservation**: Preserves Java 25, Swing FlatLaf, Jackson, TCP sockets, Spring JDBC, jBCrypt. No WebSocket or REST added.
- [x] **Principle IV: Protocol & Data Integrity**: Preserves existing `RequestEnvelope`, `ResponseEnvelope`, `LoginRequest`, `AuthenticationResult`, `AdminUserInfo`, and database schema.
- [x] **Principle V: Separation of Concerns**: Client manages presentation only; server manages authentication logic, database transactions, and authorization gating.
- [x] **Principle VI: Security by Design**: Passwords compared via BCrypt, never logged or returned. Session role verified on every administrative action.
- [x] **Principle VII: Code Style & Quality**: Conforms to `.editorconfig` and Google Java Format via Spotless.
- [x] **Principle VIII: Verification & Tests**: Test coverage with JUnit 5 unit and integration tests.
- [x] **Principle IX: Git Workflow**: Working on `feature/admin-client-v2`, meaningful commit messages.
- [x] **Principle X: AI Transparency**: Work logged in `AI-LOG.md`.

## Project Structure

### Documentation (this feature)

```text
specs/006-admin-client-v2/admin-auth/
├── spec.md              # Requirements and user scenarios
├── plan.md              # Implementation architecture plan
├── research.md          # Architectural decisions & research
├── data-model.md        # Wire DTOs, session context, and DB tables
├── quickstart.md        # Test scenarios and execution guide
├── contracts/
│   └── auth-contracts.md # Envelopes, request/response formats, error mapping
└── tasks.md             # Implementation tasks ($speckit-tasks output)
```

### Source Code

```text
common/
└── src/main/java/com/chat/common/
    ├── dto/auth/LoginRequest.java
    ├── dto/auth/AuthenticationResult.java
    ├── dto/admin/AdminUserInfo.java
    ├── dto/social/UserTarget.java
    └── enums/ActionType.java, ErrorCode.java, UserRole.java

server/
├── src/main/java/com/chat/server/
│   ├── session/
│   │   ├── SessionContext.java         # Updated to Long userId & loginHistoryId
│   │   └── OnlineSessionRegistry.java  # Updated to Long userId
│   ├── handler/
│   │   ├── ActionDispatcher.java       # Role gating (already enforces ADMIN)
│   │   └── impl/
│   │       ├── LoginHandler.java       # ActionType.LOGIN implementation
│   │       ├── LogoutHandler.java      # ActionType.LOGOUT implementation
│   │       └── AdminGetUserHandler.java# ActionType.ADMIN_GET_USER implementation
└── src/test/java/com/chat/server/
    └── handler/
        ├── LoginHandlerTest.java
        ├── LogoutHandlerTest.java
        └── AdminGetUserHandlerTest.java

admin-client/
├── src/main/java/com/chat/admin/
│   ├── controller/
│   │   └── AdminAuthController.java    # SocketClient orchestration & two-step verify
│   ├── view/
│   │   ├── AdminLoginDialog.java       # Async login call, button disable, error label
│   │   └── AdminMainFrame.java         # Dispatches LOGOUT on sign out
└── src/test/java/com/chat/admin/
    └── controller/
        └── AdminAuthControllerTest.java
```

**Structure Decision**: Code changes are strictly isolated to `server` handlers/session and `admin-client` controller/views. `common` is unmodified.

## Complexity Tracking

No constitution violations.
