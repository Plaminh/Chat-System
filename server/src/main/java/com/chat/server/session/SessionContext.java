package com.chat.server.session;

import com.chat.common.enums.UserRole;
import java.time.Instant;
import java.util.Objects;

public class SessionContext {
    private final String connectionId;
    private volatile Long userId;
    private volatile String username;
    private volatile UserRole role;
    private volatile Long loginHistoryId;
    private volatile Instant authenticatedAt;

    public SessionContext(String connectionId) {
        this.connectionId = Objects.requireNonNull(connectionId, "connectionId");
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getLoginHistoryId() {
        return loginHistoryId;
    }

    public void setLoginHistoryId(Long loginHistoryId) {
        this.loginHistoryId = loginHistoryId;
    }

    public Instant getAuthenticatedAt() {
        return authenticatedAt;
    }

    public boolean isAuthenticated() {
        return userId != null && role != null;
    }

    public boolean hasRole(UserRole requiredRole) {
        return isAuthenticated() && this.role == requiredRole;
    }

    public synchronized void authenticate(Long userId, String username, UserRole role) {
        authenticate(userId, username, role, null);
    }

    public synchronized void authenticate(Long userId, String username, UserRole role, Long loginHistoryId) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.username = Objects.requireNonNull(username, "username");
        this.role = Objects.requireNonNull(role, "role");
        this.loginHistoryId = loginHistoryId;
        this.authenticatedAt = Instant.now();
    }

    public synchronized void clear() {
        this.userId = null;
        this.username = null;
        this.role = null;
        this.loginHistoryId = null;
        this.authenticatedAt = null;
    }
}
