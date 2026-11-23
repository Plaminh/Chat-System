# Research: Server Core Platform Architecture & Decisions

**Feature**: `specs/005-server-core` | **Date**: 2026-11-23

## Research Questions & Architectural Decisions

### 1. Jackson ObjectMapper & Date Formatting Configuration

- **Context**: All protocol communication uses ISO-8601 strings for `LocalDate` and `OffsetDateTime`. Jackson's default behavior serializes dates as numeric timestamps or arrays if not configured, which breaks protocol contracts.
- **Decision**: Configure `ProtocolConfig` using `JsonMapper.builder()`:
  - Add `JavaTimeModule()`.
  - Disable `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`.
  - Disable `DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE`.
  - Do NOT enable unknown-enum fallbacks (`READ_UNKNOWN_ENUM_VALUES_AS_NULL`); unknown action types must throw protocol errors.
- **Rationale**: Strict alignment with `specs/004-common-v2/quickstart.md` guarantees identical serialization across Common, Server, Admin Client, and User Client.

---

### 2. Socket Concurrency & Framing Architecture

- **Context**: Java TCP sockets receive byte streams. Clients and server exchange newline-delimited JSON (NDJSON) messages limited to 64 KB (`ProtocolConstants.MAX_MESSAGE_CHARS`).
- **Decision**:
  - Maintain the existing `acceptLoop` in `TcpSocketServer` with dedicated worker thread pool (`ThreadPoolExecutor`).
  - Use `JsonLineReader.readLine(reader, ProtocolConstants.MAX_MESSAGE_CHARS)` to enforce frame bounds.
  - When a message exceeds 64 KB, catch `MessageTooLargeException`, return a `ResponseEnvelope` with `ErrorCode.MESSAGE_TOO_LARGE`, and close the connection.
  - Wrap client socket write operations with thread synchronization to prevent interleaved JSON lines when concurrent responses and events are pushed simultaneously.
- **Rationale**: Prevents denial-of-service through memory exhaustion and eliminates race conditions during unsolicited server pushes.

---

### 3. Session Management & Role-Based Authorization

- **Context**: HTTP/REST sessions or cookies do not exist over raw TCP sockets. Each persistent TCP connection must track who is currently logged in.
- **Decision**:
  - Introduce `SessionContext` tied to the lifecycle of a `ClientConnection`.
  - Initially, a connection is `UNAUTHENTICATED` (`userId = null`, `role = null`).
  - Upon successful `LOGIN` or `ADMIN_LOGIN`, the session is mutated/promoted with `userId`, `username`, and `UserRole` (`USER` or `ADMIN`).
  - Before invoking an `ActionHandler`, `ActionDispatcher` performs security gating:
    - If action is public (`PING`, `LOGIN`, `ADMIN_LOGIN`, `REGISTER`, `RECOVER_PASSWORD`), permit execution.
    - If action is protected and session is unauthenticated, immediately return `ErrorCode.UNAUTHENTICATED`.
    - If action starts with `ADMIN_` and session role is not `ADMIN`, immediately return `ErrorCode.UNAUTHORIZED`.
- **Rationale**: Centralized authorization prevents repetitive security checks inside individual handlers.

---

### 4. Modular Action Dispatching Pattern

- **Context**: The server must support 60 distinct `ActionType` values mapped across different functional issues (A0-A9, U1-U12, CORE). Hardcoding a monolithic switch statement in `RequestHandler` violates single-responsibility and causes merge conflicts.
- **Decision**:
  - Define an interface `ActionHandler`:
    ```java
    public interface ActionHandler {
        ActionType action();
        ResponseEnvelope handle(RequestEnvelope request, SessionContext session);
    }
    ```
  - `ActionDispatcher` automatically collects all Spring beans implementing `ActionHandler` into an `EnumMap<ActionType, ActionHandler>`.
  - If a requested `ActionType` has no registered bean, `ActionDispatcher` returns `ResponseEnvelope` with `ErrorCode.UNSUPPORTED_OPERATION` and message `"Operation not implemented yet"`.
- **Rationale**: Allows team members to implement independent features (e.g. `LoginHandler`, `UserListHandler`) in separate classes and branches without touching dispatcher core.

---

### 5. Online Session Tracking & Event Notification

- **Context**: The server needs to push unsolicited `EventEnvelope` instances (e.g. `PRESENCE_CHANGED`, `ACCOUNT_STATUS_CHANGED`, `NEW_MESSAGE`) to specific users or broadcast to all active connections.
- **Decision**:
  - Introduce `OnlineSessionRegistry`:
    - Thread-safe storage: `ConcurrentHashMap<UUID, Set<ClientConnection>>` (allowing a user to connect from multiple sessions or single session).
    - Expose methods: `register(userId, connection)`, `unregister(connection)`, `sendToUser(UUID userId, EventEnvelope event)`, `sendToUsers(Set<UUID> userIds, EventEnvelope event)`, and `broadcast(EventEnvelope event)`.
  - When a socket disconnects, `TcpSocketServer` automatically calls `unregister(connection)` and triggers a presence update event notifying friends that the user is now offline.
- **Rationale**: Fully thread-safe, decoupled event routing that does not block worker threads.

---

### 6. Client-Side Socket Networking Adapter (`SocketClient`)

- **Context**: Both `user-client` and `admin-client` need to send requests and wait for responses, while concurrently listening for push events, all without freezing the Swing UI event dispatch thread (EDT).
- **Decision**:
  - Create a reusable `SocketClient` component:
    - Maintains an active TCP `Socket`.
    - `sendAsync(ActionType action, Object payload)` returns `CompletableFuture<ResponseEnvelope>`. It generates a unique `UUID` `requestId` and stores it in `ConcurrentHashMap<String, CompletableFuture<ResponseEnvelope>> pendingRequests`.
    - Runs a dedicated background listener thread: reads lines using `JsonLineReader`.
    - If the incoming message is a `ResponseEnvelope` (has `requestId`), completes the matching `CompletableFuture`.
    - If the incoming message is an `EventEnvelope` (has `eventId`), dispatches it to registered `EventListener` callbacks (routed to Swing `SwingUtilities.invokeLater` if updating UI).
- **Rationale**: Eliminates code duplication across `user-client` and `admin-client` and ensures thread safety between network I/O and Swing UI.

---

### 7. Centralized Exception Translation

- **Context**: Backend services and repositories throw domain and database exceptions (`InvalidSortFieldException`, `DuplicateKeyException`, `ProtocolValidationException`). These must be translated into standardized protocol `ErrorCode` values without leaking raw SQL or stack traces.
- **Decision**:
  - `ActionDispatcher` wraps handler invocations in a standard exception translation boundary:
    - `InvalidSortFieldException` $\rightarrow$ `ErrorCode.VALIDATION_FAILED` (e.g., "Invalid sort field: ...")
    - `DuplicateKeyException` $\rightarrow$ `ErrorCode.ALREADY_EXISTS` or `CONFLICT`
    - `ProtocolValidationException` $\rightarrow$ `ErrorCode.INVALID_PAYLOAD`
    - `IllegalArgumentException` $\rightarrow$ `ErrorCode.INVALID_REQUEST`
    - Unexpected `Exception` $\rightarrow$ `ErrorCode.INTERNAL_ERROR` (with generic safe message, logging full stack trace on server).
- **Rationale**: Complies with Assignment Security requirements: no database credentials, raw query strings, or server stack traces are disclosed over the wire.
