package com.chat.server.session;

import com.chat.common.enums.UserRole;
import java.time.Instant;
import java.util.Objects;

public class SessionContext {
    private final String connectionId;
    private volatile Long userId;
    private volatile String username;
    private volatile UserRole role;
    private volatile Instant authenticatedAt;
    private volatile Long databaseUserId;
    private volatile Long loginHistoryId;

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

    public Instant getAuthenticatedAt() {
        return authenticatedAt;
    }

    public boolean isAuthenticated() {
        return userId != null && role != null;
    }

    public boolean hasRole(UserRole requiredRole) {
        return isAuthenticated() && this.role == requiredRole;
    }

    public synchronized void authenticate(long userId, String username, UserRole role) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        this.userId = userId;
        this.username = Objects.requireNonNull(username, "username");
        this.role = Objects.requireNonNull(role, "role");
        this.authenticatedAt = Instant.now();
    }

    public synchronized void setDatabaseSession(long databaseUserId, long loginHistoryId) {
        this.databaseUserId = databaseUserId;
        this.loginHistoryId = loginHistoryId;
    }

    public Long getDatabaseUserId() {
        return databaseUserId;
    }

    public Long getLoginHistoryId() {
        return loginHistoryId;
    }

    public void clearDatabaseSession() {
        this.databaseUserId = null;
        this.loginHistoryId = null;
    }

    public synchronized void clear() {
        this.userId = null;
        this.username = null;
        this.role = null;
        this.authenticatedAt = null;
    }
}
