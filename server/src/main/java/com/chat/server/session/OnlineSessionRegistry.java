package com.chat.server.session;

import com.chat.common.dto.EventEnvelope;
import com.chat.server.socket.ClientConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineSessionRegistry {
    private static final Logger log = LoggerFactory.getLogger(OnlineSessionRegistry.class);

    private final Map<Long, Set<ClientConnection>> activeUserSessions = new ConcurrentHashMap<>();
    private final Set<ClientConnection> allConnections = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper;

    public OnlineSessionRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void registerConnection(ClientConnection connection) {
        allConnections.add(connection);
    }

    public void unregisterConnection(ClientConnection connection) {
        allConnections.remove(connection);
        activeUserSessions.values().forEach(set -> set.remove(connection));
        activeUserSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void bindUser(Long userId, ClientConnection connection) {
        if (userId != null) {
            activeUserSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(connection);
        }
    }

    public void unbindUser(Long userId, ClientConnection connection) {
        if (userId != null) {
            activeUserSessions.computeIfPresent(userId, (k, set) -> {
                set.remove(connection);
                return set.isEmpty() ? null : set;
            });
        }
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<ClientConnection> connections = activeUserSessions.get(userId);
        return connections != null && !connections.isEmpty();
    }

    public Set<Long> getOnlineUserIds() {
        return Collections.unmodifiableSet(activeUserSessions.keySet());
    }

    public boolean sendToUser(Long userId, EventEnvelope event) {
        if (userId == null) {
            return false;
        }
        Set<ClientConnection> connections = activeUserSessions.get(userId);
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        boolean sent = false;
        for (ClientConnection conn : connections) {
            try {
                conn.sendEnvelope(mapper, event);
                sent = true;
            } catch (IOException ex) {
                log.warn("Failed to push event {} to user session {}", event.type(), userId, ex);
            }
        }
        return sent;
    }

    public int sendToUsers(Set<Long> userIds, EventEnvelope event) {
        if (userIds == null || userIds.isEmpty()) return 0;
        int count = 0;
        for (Long userId : userIds) {
            if (sendToUser(userId, event)) {
                count++;
            }
        }
        return count;
    }

    public void broadcast(EventEnvelope event) {
        for (ClientConnection conn : allConnections) {
            try {
                conn.sendEnvelope(mapper, event);
            } catch (IOException ex) {
                log.warn("Failed to broadcast event {}", event.type(), ex);
            }
        }
    }

    public int getConnectedClientsCount() {
        return allConnections.size();
    }
}
