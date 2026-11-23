package com.chat.user.network;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SocketClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private ServerSocket serverSocket;
    private Thread mockServerThread;
    private SocketClient client;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
        client = new SocketClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) client.close();
        if (serverSocket != null) serverSocket.close();
    }

    @Test
    void sendsRequestAndCorrelatesResponseAsync() throws Exception {
        mockServerThread = new Thread(() -> {
            try (Socket serverSide = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(serverSide.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSide.getOutputStream(), StandardCharsets.UTF_8))) {

                String requestLine = reader.readLine();
                var tree = mapper.readTree(requestLine);
                String reqId = tree.get("requestId").asText();

                ResponseEnvelope response = new ResponseEnvelope(reqId, true, mapper.createObjectNode().put("result", "ok"), null, null);
                writer.write(mapper.writeValueAsString(response));
                writer.newLine();
                writer.flush();
            } catch (Exception ignored) {}
        });
        mockServerThread.start();

        client.connect("127.0.0.1", serverSocket.getLocalPort());
        assertTrue(client.isConnected());

        CompletableFuture<ResponseEnvelope> future = client.sendAsync(ActionType.PING, null);
        ResponseEnvelope response = future.get(5, TimeUnit.SECONDS);

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("ok", response.data().get("result").asText());
    }

    @Test
    void receivesIncomingPushEventViaListener() throws Exception {
        CountDownLatch eventLatch = new CountDownLatch(1);
        AtomicReference<EventEnvelope> receivedEvent = new AtomicReference<>();

        mockServerThread = new Thread(() -> {
            try (Socket serverSide = serverSocket.accept();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSide.getOutputStream(), StandardCharsets.UTF_8))) {

                EventEnvelope event = new EventEnvelope("event-1", MessageType.ACCOUNT_STATUS_CHANGED, mapper.createObjectNode().put("status", "LOCKED"));
                writer.write(mapper.writeValueAsString(event));
                writer.newLine();
                writer.flush();
            } catch (Exception ignored) {}
        });
        mockServerThread.start();

        client.setEventListener(event -> {
            receivedEvent.set(event);
            eventLatch.countDown();
        });

        client.connect("127.0.0.1", serverSocket.getLocalPort());

        boolean received = eventLatch.await(5, TimeUnit.SECONDS);
        assertTrue(received);
        assertNotNull(receivedEvent.get());
        assertEquals(MessageType.ACCOUNT_STATUS_CHANGED, receivedEvent.get().type());
    }
}
