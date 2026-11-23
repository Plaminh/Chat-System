package com.chat.server.socket;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.MessageType;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.handler.RequestHandler;
import com.chat.server.handler.impl.PingHandler;
import com.chat.server.session.OnlineSessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
        ActionDispatcher dispatcher = new ActionDispatcher(mapper, List.of(pingHandler));
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
}
