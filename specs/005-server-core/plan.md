# Implementation Plan: Server Core Platform (Issue #3)

**Branch**: `feature/server-core` | **Date**: 2026-11-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/005-server-core/spec.md`

## Summary

Build the foundational TCP socket communication and protocol execution infrastructure for the Java Chat System backend. This includes configuring Jackson ISO-8601 time handling (`ProtocolConfig`), establishing thread-safe connection session state (`SessionContext`, `ClientConnection`), centralizing security authorization and action routing (`ActionDispatcher`, `ActionHandler`), implementing real-time event broadcasting and online presence tracking (`OnlineSessionRegistry`), and providing a reusable non-blocking socket client adapter (`SocketClient`) for desktop clients.

## Technical Context

**Language/Version**: Java 25 LTS

**Primary Dependencies**: Spring Boot (non-web mode), Spring Context, Jackson Databind, Jackson DataType JSR310 (`JavaTimeModule`), SLF4J / Logback, `com.chat:common`

**Storage**: In-memory concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArraySet`) for active sessions and online presence; existing PostgreSQL repositories accessed via Spring JDBC when actions are delegated to handlers.

**Testing**: JUnit 5, AssertJ, Spring Boot Test, Java TCP Socket loopback integration tests

**Target Platform**: Windows 10/11 x64 desktop runtime and server JVM

**Project Type**: Multi-module Java desktop application and backend TCP service

**Performance Goals**: Sub-5ms ping loopback latency, support 50+ concurrent socket connections without connection dropping or frame interleaving

**Constraints**: Max frame size 64 KB (`ProtocolConstants.MAX_MESSAGE_CHARS`), no WebSocket, no HTTP/REST client protocol, raw TCP sockets with newline-delimited JSON (NDJSON)

**Scale/Scope**: 60 protocol actions, real-time push notification support, multi-client synchronization

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evaluation |
| :--- | :---: | :--- |
| **I. Project Scope** | PASS | Implements only requirements specified in university Issue #3 ([CORE] Nền tảng server) and `specs/004-common-v2`. No unapproved business features added. |
| **II. Technology Constraints** | PASS | Uses raw TCP sockets (`ServerSocket`, `Socket`) and Spring Boot in non-web mode (`WebApplicationType.NONE`). No WebSocket, no REST client protocol. |
| **III. Existing Stack** | PASS | Preserves Java 25 LTS, Maven, Spring Boot, Jackson, and Spring JDBC. No new dependencies or framework replacements. |
| **IV. Module Boundaries** | PASS | Shared wire models and client networking remain in `common` or client modules. Server does not depend on clients; clients do not depend on `server`. |
| **V. Architecture Responsibilities** | PASS | Server owns authorization, session validation, request routing, and networking state. Frontends handle presentation only. |
| **VI. Code Quality** | PASS | Modular `ActionHandler` pattern avoids monolithic switches, eliminates code duplication, and respects single responsibility. |
| **VII. Testing** | PASS | Loopback socket integration tests verify framing, ping/pong, security gating, and concurrent push events. `mvn test` passes cleanly. |
| **VIII. Development Workflow** | PASS | Branched from `dev` as `feature/server-core`. All changes committed incrementally before merge. |
| **IX. AI Usage** | PASS | Specification, design, and implementation recorded in `AI-LOG.md`. |
| **X. Project Delivery** | PASS | Fulfills Version 2 core platform requirements ahead of the 25/11 deadline. |

## Project Structure

### Documentation (this feature)

```text
specs/005-server-core/
├── plan.md              # Implementation plan (this file)
├── research.md          # Architectural research & technical decisions
├── data-model.md        # In-memory session and component models
├── quickstart.md        # Integration guide & handler examples
├── checklists/
│   └── requirements.md  # Specification quality checklist
└── contracts/
    └── core-contracts.md # Protocol framing, authorization, & error mapping
```

### Source Code Layout

```text
common/
└── src/main/java/com/chat/common/
    ├── dto/             # RequestEnvelope, ResponseEnvelope, EventEnvelope
    ├── enums/           # ActionType, ErrorCode, MessageType, UserRole
    └── protocol/        # JsonLineReader, ProtocolValidator, ProtocolConstants

server/
└── src/main/java/com/chat/server/
    ├── ChatServerApplication.java
    ├── config/
    │   ├── DatabaseConfig.java
    │   └── ProtocolConfig.java          # Jackson ISO-8601 & JavaTimeModule configuration
    ├── socket/
    │   ├── TcpSocketServer.java         # Connection listener & worker thread pool
    │   └── ClientConnection.java       # Socket wrapper with atomic write synchronization
    ├── session/
    │   ├── SessionContext.java          # In-memory identity & authentication state
    │   └── OnlineSessionRegistry.java   # Online user tracking & event push router
    ├── handler/
    │   ├── ActionHandler.java           # Modular action execution interface
    │   ├── ActionDispatcher.java        # Security gating & action routing
    │   ├── RequestHandler.java          # Wire protocol adapter
    │   └── impl/
    │       └── PingHandler.java         # PING -> PONG handler implementation
    └── repository/                      # Pre-existing DAO layer (from feature/server-dao)

server/
└── src/test/java/com/chat/server/
    ├── socket/
    │   └── TcpSocketServerIntegrationTest.java # Full TCP socket loopback tests
    └── handler/
        └── ActionDispatcherTest.java          # Security & routing unit tests
```

## Complexity Tracking

> **Constitution check passes with zero violations. No unjustified complexity introduced.**

| Aspect | Decision | Justification |
| :--- | :--- | :--- |
| Action Dispatching | Spring `@Component` `ActionHandler` registry | Avoids a 60-branch switch statement in a single class; allows parallel development by team members without merge conflicts. |
| Concurrency | Atomic `writeLock` in `ClientConnection` | Prevents corrupted NDJSON frames when server push events occur during request response writing. |
