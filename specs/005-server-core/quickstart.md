# Quickstart: Server Core Platform Integration

**Feature**: `specs/005-server-core` | **Date**: 2026-11-23

## 1. Server Configuration & Startup

### Start Server
Server configuration is loaded via Spring Boot non-web application:
```java
// ChatServerApplication.java
@SpringBootApplication
public class ChatServerApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChatServerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
```
`TcpSocketServer` automatically binds to `chat.server.port` (default `5050`) on application start.

---

## 2. Implementing a New ActionHandler

When implementing a business feature (e.g. Issue #7 Admin Login, or Issue #6 User Management), simply create a Spring `@Component` implementing `ActionHandler`:

```java
@Component
public class AdminLoginHandler implements ActionHandler {

    private final ObjectMapper mapper;
    private final AuthService authService;

    public AdminLoginHandler(ObjectMapper mapper, AuthService authService) {
        this.mapper = mapper;
        this.authService = authService;
    }

    @Override
    public ActionType action() {
        return ActionType.ADMIN_LOGIN;
    }

    @Override
    public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
        LoginRequest payload = ProtocolValidator.decodePayload(mapper, request, LoginRequest.class);
        AuthenticationResult result = authService.authenticateAdmin(payload.username(), payload.password());

        // Promote session identity upon successful authentication
        session.authenticate(result.userId(), result.username(), UserRole.ADMIN);

        return new ResponseEnvelope(
            request.requestId(),
            true,
            mapper.valueToTree(result),
            null,
            null
        );
    }
}
```
`ActionDispatcher` automatically discovers `AdminLoginHandler` and registers it without modifying dispatcher source code.

---

## 3. Pushing Events from Server

To push real-time events to connected clients from any service:

```java
@Service
public class PresenceService {

    private final OnlineSessionRegistry sessionRegistry;

    public PresenceService(OnlineSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void notifyPresenceChange(UUID userId, PresenceStatus status) {
        PresenceUpdate update = new PresenceUpdate(userId, status, OffsetDateTime.now(ZoneOffset.UTC));
        EventEnvelope event = new EventEnvelope(
            UUID.randomUUID().toString(),
            MessageType.PRESENCE_CHANGED,
            mapper.valueToTree(update)
        );

        // Broadcast to all active sessions or targeted friend sessions
        sessionRegistry.broadcast(event);
    }
}
```

---

## 4. Client Usage Example (`SocketClient`)

From `admin-client` or `user-client`:

```java
SocketClient client = new SocketClient("localhost", 5050);
client.connect();

// Register listener for incoming server push events
client.setEventListener(event -> {
    if (event.type() == MessageType.PRESENCE_CHANGED) {
        PresenceUpdate presence = mapper.treeToValue(event.data(), PresenceUpdate.class);
        SwingUtilities.invokeLater(() -> uiModel.updatePresence(presence));
    }
});

// Send request asynchronously
client.sendAsync(ActionType.PING, null)
    .thenAccept(response -> {
        System.out.println("Received: " + response.data().get("message").asText()); // PONG
    })
    .exceptionally(ex -> {
        System.err.println("Request failed: " + ex.getMessage());
        return null;
    });
```
