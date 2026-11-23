# Data Model & Class Design: Server Core Platform

**Feature**: `specs/005-server-core` | **Date**: 2026-11-23

## Server In-Memory Models & Core Components

```mermaid
classDiagram
    class TcpSocketServer {
        -int configuredPort
        -ServerSocket serverSocket
        -ExecutorService acceptor
        -ThreadPoolExecutor workers
        -OnlineSessionRegistry sessionRegistry
        -ActionDispatcher dispatcher
        +start() void
        +stop() void
        -acceptLoop() void
        -serve(Socket client) void
    }

    class ClientConnection {
        -String connectionId
        -Socket socket
        -BufferedWriter writer
        -SessionContext sessionContext
        -Object writeLock
        +send(String jsonLine) void
        +sendEnvelope(Object envelope) void
        +close() void
        +getSessionContext() SessionContext
    }

    class SessionContext {
        -String connectionId
        -UUID userId
        -String username
        -UserRole role
        -Instant authenticatedAt
        +isAuthenticated() boolean
        +hasRole(UserRole role) boolean
        +authenticate(UUID userId, String username, UserRole role) void
        +clear() void
    }

    class OnlineSessionRegistry {
        -Map~UUID, Set~ClientConnection~~ activeSessions
        -Set~ClientConnection~ anonymousConnections
        +registerAuthenticated(UUID userId, ClientConnection conn) void
        +unregister(ClientConnection conn) Optional~UUID~
        +isUserOnline(UUID userId) boolean
        +sendToUser(UUID userId, EventEnvelope event) boolean
        +sendToUsers(Set~UUID~ userIds, EventEnvelope event) int
        +broadcast(EventEnvelope event) void
        +getOnlineUserIds() Set~UUID~
    }

    class ActionDispatcher {
        -Map~ActionType, ActionHandler~ handlers
        -ObjectMapper mapper
        +dispatch(RequestEnvelope request, SessionContext session) ResponseEnvelope
        -isAuthorized(ActionType action, SessionContext session) boolean
    }

    class ActionHandler {
        <<interface>>
        +action() ActionType
        +handle(RequestEnvelope request, SessionContext session) ResponseEnvelope
    }

    TcpSocketServer --> ClientConnection : manages
    ClientConnection *-- SessionContext : holds
    TcpSocketServer --> OnlineSessionRegistry : tracks online users
    TcpSocketServer --> ActionDispatcher : routes requests
    ActionDispatcher o-- ActionHandler : dispatches to
```

### Component Details

#### 1. `SessionContext`
- **Location**: `com.chat.server.session.SessionContext`
- **Purpose**: Encapsulates authentication state and security identity associated with a single TCP socket connection.
- **Fields**:
  - `connectionId` (String): Unique identifier for the socket connection (e.g., UUID).
  - `userId` (UUID): Unique user ID in database once authenticated (null if anonymous).
  - `username` (String): Username once authenticated (null if anonymous).
  - `role` (UserRole): Role from `common` (`USER` or `ADMIN`).
  - `authenticatedAt` (Instant): Timestamp when authentication succeeded.
- **State Invariants**:
  - `isAuthenticated()` is true if and only if `userId != null && role != null`.

#### 2. `ClientConnection`
- **Location**: `com.chat.server.socket.ClientConnection`
- **Purpose**: Thread-safe abstraction wrapping a client `Socket`, its output stream, and its associated `SessionContext`.
- **Concurrency Guard**:
  - Writes to `BufferedWriter` are synchronized via an internal `writeLock` so that response frames and server push event frames never interleave.

#### 3. `OnlineSessionRegistry`
- **Location**: `com.chat.server.session.OnlineSessionRegistry`
- **Purpose**: Tracks active user sessions and routes push events.
- **Storage**:
  - `ConcurrentHashMap<UUID, Set<ClientConnection>>`: Maps a `userId` to active connections. Supports multiple simultaneous logins by the same user if allowed, or tracks all sockets.
  - `Set<ClientConnection>`: Tracks connected but unauthenticated sockets.
- **Key Methods**:
  - `sendToUser(UUID userId, EventEnvelope event)`: Serializes the event and writes it to all active sockets of the user.
  - `broadcast(EventEnvelope event)`: Sends to all authenticated sessions.

#### 4. `ActionHandler` & `ActionDispatcher`
- **Location**: `com.chat.server.handler`
- **Purpose**: Central routing of protocol requests with security validation.
- **Security Logic**:
  ```text
  If action == PING, LOGIN, ADMIN_LOGIN, REGISTER, RECOVER_PASSWORD:
      Allow execution
  Else if !session.isAuthenticated():
      Return ResponseEnvelope(id, false, null, UNAUTHENTICATED, "Authentication required")
  Else if action.name().startsWith("ADMIN_") && session.getRole() != UserRole.ADMIN:
      Return ResponseEnvelope(id, false, null, UNAUTHORIZED, "Admin privileges required")
  Else if handler == null:
      Return ResponseEnvelope(id, false, null, UNSUPPORTED_OPERATION, "Operation not implemented yet")
  Else:
      Execute handler.handle(request, session)
  ```

---

## Client-Side Networking Adapter (`SocketClient`)

```mermaid
classDiagram
    class SocketClient {
        -String host
        -int port
        -Socket socket
        -BufferedWriter writer
        -BufferedReader reader
        -ObjectMapper mapper
        -Map~String, CompletableFuture~ResponseEnvelope~~ pendingRequests
        -Consumer~EventEnvelope~ eventListener
        -ExecutorService readerThread
        -volatile boolean connected
        +connect() void
        +disconnect() void
        +sendAsync(ActionType action, Object payload) CompletableFuture~ResponseEnvelope~
        +sendSync(ActionType action, Object payload, Duration timeout) ResponseEnvelope
        +setEventListener(Consumer~EventEnvelope~ listener) void
        -listenLoop() void
    }
```

- **Location**: `com.chat.client.net.SocketClient` (or package in `common` / client module)
- **Fields**:
  - `pendingRequests`: `ConcurrentHashMap<String, CompletableFuture<ResponseEnvelope>>` indexed by `requestId`.
  - `eventListener`: Callback executed when an `EventEnvelope` arrives.
- **Thread Safety**:
  - `sendAsync` synchronizes writing to socket.
  - `listenLoop` continuously reads frames on a dedicated background thread, safely dispatching events and completing futures.
