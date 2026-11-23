# Feature Specification: Server Core Platform (Nền tảng Server)

**Feature Branch**: `feature/server-core`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "áp dụng speckit, specify ra trước phần CORE nha! Nền tảng server á! Và bạn xem thử có cấu hình socket hay gì chưa? Bên cạnh đó tôi cần phải tải docker và làm như nào với docker? Lời nhắn từ Plaminh: t làm xog phần DAO rồi nha, file env thì m tạo từ file .env.example đổi mật khẩu lại để chạy docker container trên máy m nha, m dùng dbeaver mở container đó ra và xem bảng khi thực hiện các tính năng nha. Hiện phần server core vẫn chưa xong hết, t có note lại trong issue thì phần đó m làm tiếp nha. Cho tôi hướng đi tiếp"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Secure Request Dispatching & Session Management (Priority: P1)

As a desktop client (User Client or Admin Client), I need to establish a stable TCP socket connection to the server, send serialized JSON protocol requests, and receive correlated responses so that I can authenticate, invoke operations, and enforce role-based access controls without connection collisions.

**Why this priority**: Without request dispatching and session management, no client can communicate with the server or execute any business feature.

**Independent Test**: Can be tested by connecting a test socket client, sending a `PING` request to receive `PONG`, logging in to establish a session, and verifying that authenticated actions succeed while unauthorized or unauthenticated actions are rejected with exact error codes.

**Acceptance Scenarios**:
1. **Given** an unauthenticated TCP connection, **When** the client sends a `PING` action, **Then** the server responds with success and `{"message": "PONG"}` with matching `requestId`.
2. **Given** an unauthenticated TCP connection, **When** the client attempts an action requiring authentication (e.g., `GET_PROFILE` or `ADMIN_GET_USERS`), **Then** the server responds with `success: false`, `errorCode: UNAUTHENTICATED`, and safe error message.
3. **Given** a TCP connection authenticated as role `USER`, **When** the client attempts any `ADMIN_*` action, **Then** the server responds with `success: false`, `errorCode: UNAUTHORIZED`.
4. **Given** a valid request with an unknown or unimplemented `ActionType`, **Then** the server responds with `success: false` and `errorCode: UNSUPPORTED_OPERATION`.

---

### User Story 2 - Real-time Event Notification & Online Presence Tracking (Priority: P2)

As a connected chat user or admin, I need the server to track my active online status and dispatch asynchronous events (such as presence changes, account locks, or group membership updates) to my client in real-time without polling.

**Why this priority**: Immediate feedback and multi-client synchronization are required for chat applications and administrative moderation actions (e.g., locking an abusive account immediately terminates their session).

**Independent Test**: Can be tested by connecting two concurrent client sockets: when Client A connects and authenticates, Client B receives a `PRESENCE_CHANGED` event indicating Client A is ONLINE. When Client A disconnects, Client B receives a presence update indicating Client A is OFFLINE.

**Acceptance Scenarios**:
1. **Given** two active connections, **When** User A authenticates, **Then** User A is registered in the active session registry, and relevant connected peers receive a `PRESENCE_CHANGED` event.
2. **Given** an authenticated user whose account status changes to `LOCKED` by an admin, **Then** an `ACCOUNT_STATUS_CHANGED` event is pushed to that user's socket and their session is terminated.
3. **Given** an active connection, **When** the client abruptly disconnects or closes the socket, **Then** the server cleans up the session registry and marks the user's presence as OFFLINE.

---

### User Story 3 - Robust Client-Side Socket Networking Adapter (Priority: P3)

As a frontend developer on `admin-client` or `user-client`, I need a reusable, non-blocking networking client that correlates asynchronous responses to pending requests using `requestId` and routes incoming server push events to dedicated background event listeners.

**Why this priority**: Both Swing frontends require a responsive UI thread that does not freeze during socket operations, and they need a standardized way to receive push events.

**Independent Test**: Can be tested by sending concurrent asynchronous requests from the client adapter, verifying that each response is matched to its corresponding pending future, and verifying that incoming events trigger registered event listeners.

**Acceptance Scenarios**:
1. **Given** multiple asynchronous requests fired in quick succession, **When** server responses return out-of-order, **Then** each caller receives its matching `ResponseEnvelope` by `requestId`.
2. **Given** an incoming unsolicited `EventEnvelope` from the server, **When** the client reader processes it, **Then** it does not consume any pending request correlation and dispatches the event to the registered listener.

---

### Edge Cases

- **Malformed JSON or Non-Object Payloads**: Server responds with `INVALID_JSON` or `INVALID_REQUEST` without crashing the client worker thread.
- **Oversized Message Frames**: If incoming data exceeds `ProtocolConstants.MAX_MESSAGE_CHARS` (64 KB), the server replies with `MESSAGE_TOO_LARGE` and terminates the problematic connection cleanly.
- **Unexpected Socket Disconnection**: If a client terminates unexpectedly while a request is in flight, the server catches the broken pipe, logs at debug level, cleans up the session registry, and frees worker threads.
- **Concurrent Requests on Single Socket**: Client and server socket handlers safely synchronize write operations to prevent interleaving JSON lines.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Server MUST configure Jackson `ObjectMapper` with `JavaTimeModule`, disable `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`, and disable `DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE` so all dates follow ISO-8601 text format.
- **FR-002**: Server MUST frame incoming socket messages using `JsonLineReader` and validate envelopes with `ProtocolValidator.parseRequest`.
- **FR-003**: Server MUST provide an extensible `ActionDispatcher` that maps `ActionType` to dedicated modular service handlers.
- **FR-004**: Server MUST return `UNSUPPORTED_OPERATION` error code for valid requests whose business handlers are not yet implemented.
- **FR-005**: Server MUST bind an authenticated `SessionContext` (containing `userId`, `username`, and `UserRole`) to each active TCP connection upon successful authentication.
- **FR-006**: Server MUST enforce security boundaries: reject unauthenticated calls to secured actions with `UNAUTHENTICATED`, and reject non-admin calls to `ADMIN_*` actions with `UNAUTHORIZED`.
- **FR-007**: Server MUST maintain a thread-safe registry of active user sessions (Online Users) mapped to their respective socket connections.
- **FR-008**: Server MUST provide a push notification service capable of sending `EventEnvelope` instances to specific users, a set of users, or all active sessions.
- **FR-009**: Server MUST translate backend domain exceptions into standard protocol responses:
  - `InvalidSortFieldException` -> `VALIDATION_FAILED`
  - `DuplicateKeyException` -> `ALREADY_EXISTS` or `CONFLICT`
  - Validation failures -> `VALIDATION_FAILED` or `INVALID_PAYLOAD`
- **FR-010**: Client modules MUST provide a reusable socket client adapter supporting synchronous/asynchronous request-response correlation via `requestId` and a background event receiver loop for `EventEnvelope`.

### Key Entities *(include if feature involves data)*

- **SessionContext**: Represents the state of an active socket connection. Attributes: `connectionId`, `socket`, `userId`, `username`, `role` (`USER`, `ADMIN`), `authenticatedAt`.
- **OnlineSessionRegistry**: In-memory thread-safe registry tracking active `userId` to `SessionContext` mappings, facilitating real-time event routing.
- **RequestEnvelope / ResponseEnvelope / EventEnvelope**: Standard protocol envelopes defined in the `common` module.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Basic `PING` request returns `PONG` response within 5 milliseconds on loopback connection.
- **SC-002**: Server handles at least 50 concurrent active socket connections without rejecting requests or dropping events.
- **SC-003**: 100% of unauthenticated or unauthorized requests to protected actions are strictly blocked with correct error codes (`UNAUTHENTICATED`, `UNAUTHORIZED`).
- **SC-004**: 100% of malformed JSON lines or payloads exceeding 64 KB are handled gracefully without terminating the server process.
- **SC-005**: Full existing repository test suite (`mvn clean verify`) passes with zero regressions.

## Assumptions

- PostgreSQL database is initialized via Docker Compose and accessible at `localhost:55432`.
- Business actions (User and Admin specific features) will be implemented as modular handlers plugged into the `ActionDispatcher`.
- Passwords and sensitive credentials are encrypted using BCrypt in the service layer and never logged or serialized in responses.
