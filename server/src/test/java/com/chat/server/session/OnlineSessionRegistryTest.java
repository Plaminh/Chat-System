package com.chat.server.session;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.enums.MessageType;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.socket.ClientConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OnlineSessionRegistryTest {
    private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
    private OnlineSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new OnlineSessionRegistry(mapper);
    }

    @Test
    void registersAndBindsUserSuccessfully() throws Exception {
        long userId = 42L;
        SessionContext session = new SessionContext("conn-1");
        session.authenticate(userId, "testuser", com.chat.common.enums.UserRole.USER);
        StringWriter sw = new StringWriter();
        ClientConnection connection = new ClientConnection(new Socket(), new BufferedWriter(sw), session);

        registry.registerConnection(connection);
        registry.bindUser(userId, connection);

        assertTrue(registry.isUserOnline(userId));
        assertEquals(Set.of(userId), registry.getOnlineUserIds());
        assertEquals(1, registry.getConnectedClientsCount());

        EventEnvelope event = new EventEnvelope("ev-1", MessageType.PRESENCE_CHANGED, mapper.createObjectNode().put("status", "ONLINE"));
        boolean sent = registry.sendToUser(userId, event);
        assertTrue(sent);
        assertTrue(sw.toString().contains("\"PRESENCE_CHANGED\""));

        registry.unregisterConnection(connection);
        assertFalse(registry.isUserOnline(userId));
        assertEquals(0, registry.getConnectedClientsCount());
    }

    @Test
    void broadcastsToAllConnections() throws Exception {
        StringWriter sw1 = new StringWriter();
        StringWriter sw2 = new StringWriter();
        ClientConnection conn1 = new ClientConnection(new Socket(), new BufferedWriter(sw1), new SessionContext("c1"));
        ClientConnection conn2 = new ClientConnection(new Socket(), new BufferedWriter(sw2), new SessionContext("c2"));

        registry.registerConnection(conn1);
        registry.registerConnection(conn2);

        EventEnvelope event = new EventEnvelope("ev-b", MessageType.PRESENCE_CHANGED, mapper.createObjectNode().put("info", "test"));
        registry.broadcast(event);

        assertTrue(sw1.toString().contains("\"ev-b\""));
        assertTrue(sw2.toString().contains("\"ev-b\""));
    }
}
