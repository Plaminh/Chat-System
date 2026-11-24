package com.chat.server.socket;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.MessageType;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.handler.ActionHandler;
import com.chat.server.handler.RequestHandler;
import com.chat.server.handler.impl.PingHandler;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TcpSocketServerIntegrationTest {
    private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
    private TcpSocketServer server;
    private OnlineSessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        sessionRegistry = new OnlineSessionRegistry(mapper);
        PingHandler pingHandler = new PingHandler(mapper);
        ActionDispatcher dispatcher = new ActionDispatcher(mapper, List.of(pingHandler, new TestLoginHandler()));
        RequestHandler handler = new RequestHandler(mapper, dispatcher);
        server = new TcpSocketServer(0, handler, sessionRegistry);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void endToEndPingPongAndFramingOverSocket() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // 1. Send valid PING
            writer.write("{\"requestId\":\"test-1\",\"action\":\"PING\",\"data\":{}}\n");
            writer.flush();

            String responseLine = reader.readLine();
            assertNotNull(responseLine);
            ResponseEnvelope response = mapper.readValue(responseLine, ResponseEnvelope.class);
            assertTrue(response.success());
            assertEquals("test-1", response.requestId());
            assertEquals("PONG", response.data().get("message").asText());

            // 2. Send malformed JSON
            writer.write("{malformed-json\n");
            writer.flush();

            String errorLine = reader.readLine();
            assertNotNull(errorLine);
            ResponseEnvelope errorResp = mapper.readValue(errorLine, ResponseEnvelope.class);
            assertFalse(errorResp.success());
            assertEquals(ErrorCode.INVALID_JSON, errorResp.errorCode());
        }
    }

    @Test
    void rejectsOversizedMessageOverSocket() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // Create payload exceeding 64KB
            String hugeData = "A".repeat(70000);
            writer.write("{\"requestId\":\"huge\",\"action\":\"PING\",\"data\":{\"big\":\"" + hugeData + "\"}}\n");
            writer.flush();

            String responseLine = reader.readLine();
            assertNotNull(responseLine);
            ResponseEnvelope response = mapper.readValue(responseLine, ResponseEnvelope.class);
            assertFalse(response.success());
            assertEquals(ErrorCode.MESSAGE_TOO_LARGE, response.errorCode());
        }
    }

    @Test
    void broadcastsEventToConnectedClient() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // Wait a brief moment for connection to register
            Thread.sleep(100);

            EventEnvelope broadcastEvent = new EventEnvelope(
                    "ev-100",
                    MessageType.PRESENCE_CHANGED,
                    mapper.createObjectNode().put("status", "ONLINE")
            );
            sessionRegistry.broadcast(broadcastEvent);

            String eventLine = reader.readLine();
            assertNotNull(eventLine);
            assertTrue(eventLine.contains("\"ev-100\""));
            assertTrue(eventLine.contains("\"PRESENCE_CHANGED\""));
        }
    }

    @Test
    void sendsEventOnlyToTargetedUserWhenTwoClientsAreConnected() throws Exception {
        try (Socket aliceSocket = new Socket("127.0.0.1", server.getPort());
             Socket bobSocket = new Socket("127.0.0.1", server.getPort())) {
            BufferedWriter aliceWriter = writer(aliceSocket);
            BufferedReader aliceReader = reader(aliceSocket);
            BufferedWriter bobWriter = writer(bobSocket);
            BufferedReader bobReader = reader(bobSocket);

            assertTrue(send(aliceWriter, aliceReader, "login-a", ActionType.LOGIN, 1L).success());
            assertTrue(send(bobWriter, bobReader, "login-b", ActionType.LOGIN, 2L).success());
            assertTrue(sessionRegistry.isUserOnline(1L));
            assertTrue(sessionRegistry.isUserOnline(2L));

            EventEnvelope event = new EventEnvelope(
                    "ev-bob",
                    MessageType.PRESENCE_CHANGED,
                    mapper.createObjectNode().put("status", "ONLINE")
            );
            assertTrue(sessionRegistry.sendToUser(2L, event));

            EventEnvelope received = mapper.readValue(bobReader.readLine(), EventEnvelope.class);
            assertEquals("ev-bob", received.eventId());

            // Had the event also reached Alice, it would arrive before her PING response.
            ResponseEnvelope alicePong = send(aliceWriter, aliceReader, "ping-a", ActionType.PING, null);
            assertEquals("ping-a", alicePong.requestId());
            assertEquals("PONG", alicePong.data().get("message").asText());
        }
        // Closing the sockets takes both users offline.
        long deadline = System.currentTimeMillis() + 2000;
        while (sessionRegistry.isUserOnline(1L) && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(sessionRegistry.isUserOnline(1L));
    }

    @Test
    void servesMoreThanFourSimultaneousClients() throws Exception {
        List<Socket> sockets = new ArrayList<>();
        try {
            for (int i = 0; i < 6; i++) {
                Socket socket = new Socket("127.0.0.1", server.getPort());
                socket.setSoTimeout(3000);
                sockets.add(socket);
            }
            for (int i = 0; i < sockets.size(); i++) {
                Socket socket = sockets.get(i);
                ResponseEnvelope pong = send(writer(socket), reader(socket), "ping-" + i, ActionType.PING, null);
                assertTrue(pong.success(), "client " + i + " was not served");
            }
        } finally {
            for (Socket socket : sockets) {
                socket.close();
            }
        }
    }

    private BufferedWriter writer(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private BufferedReader reader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private ResponseEnvelope send(BufferedWriter writer, BufferedReader reader, String requestId,
                                  ActionType action, Long userId) throws IOException {
        var data = mapper.createObjectNode();
        if (userId != null) {
            data.put("userId", userId);
        }
        writer.write(mapper.writeValueAsString(new RequestEnvelope(requestId, action, data)));
        writer.newLine();
        writer.flush();
        String line = reader.readLine();
        assertNotNull(line);
        return mapper.readValue(line, ResponseEnvelope.class);
    }

    /** Stand-in for the U5 LOGIN handler: authenticates the socket's session and marks it online. */
    private final class TestLoginHandler implements ActionHandler {
        @Override
        public ActionType action() {
            return ActionType.LOGIN;
        }

        @Override
        public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
            long userId = request.data().get("userId").asLong();
            session.authenticate(userId, "user" + userId, UserRole.USER);
            assertTrue(sessionRegistry.bindSession(session));
            return new ResponseEnvelope(request.requestId(), true, mapper.createObjectNode(), null, null);
        }
    }
}
