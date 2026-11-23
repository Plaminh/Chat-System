package com.chat.user.network;

import com.chat.common.dto.EventEnvelope;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.protocol.JsonLineReader;
import com.chat.common.protocol.ProtocolConstants;
import com.chat.common.protocol.ProtocolValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SocketClient implements AutoCloseable {
    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private final Map<String, CompletableFuture<ResponseEnvelope>> pendingRequests = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    private Socket socket;
    private BufferedWriter writer;
    private Thread readerThread;
    private Consumer<EventEnvelope> eventListener;
    private volatile boolean connected;

    public SocketClient() {}

    public synchronized void connect() throws IOException {
        if (connected) return;
        Properties config = loadConfiguration();
        String host = config.getProperty("chat.server.host", "localhost");
        int port = Integer.parseInt(config.getProperty("chat.server.port", "5050"));
        connect(host, port);
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (connected) return;
        int timeout = (int) Duration.ofSeconds(5).toMillis();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        connected = true;

        readerThread = new Thread(this::listenLoop, "user-socket-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void setEventListener(Consumer<EventEnvelope> listener) {
        this.eventListener = listener;
    }

    public CompletableFuture<ResponseEnvelope> sendAsync(ActionType action, Object payload) {
        if (!connected) {
            return CompletableFuture.failedFuture(new IOException("Socket is not connected"));
        }
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<ResponseEnvelope> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            JsonNode dataNode = payload != null ? mapper.valueToTree(payload) : mapper.createObjectNode();
            RequestEnvelope envelope = new RequestEnvelope(requestId, action, dataNode);
            String line = mapper.writeValueAsString(envelope);

            synchronized (writeLock) {
                writer.write(line);
                writer.newLine();
                writer.flush();
            }
        } catch (Exception ex) {
            pendingRequests.remove(requestId);
            future.completeExceptionally(ex);
        }
        return future;
    }

    private void listenLoop() {
        try (Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)) {
            String line;
            while (connected && (line = JsonLineReader.readLine(reader, ProtocolConstants.MAX_MESSAGE_CHARS)) != null) {
                processIncomingLine(line);
            }
        } catch (IOException ignored) {
        } finally {
            close();
        }
    }

    private void processIncomingLine(String line) {
        try {
            JsonNode tree = mapper.readTree(line);
            if (tree != null && tree.hasNonNull("eventId")) {
                EventEnvelope event = ProtocolValidator.parseEvent(mapper, line);
                Consumer<EventEnvelope> listener = this.eventListener;
                if (listener != null) {
                    listener.accept(event);
                }
            } else if (tree != null && tree.hasNonNull("requestId")) {
                ResponseEnvelope response = mapper.readValue(line, ResponseEnvelope.class);
                CompletableFuture<ResponseEnvelope> future = pendingRequests.remove(response.requestId());
                if (future != null) {
                    future.complete(response);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public ResponseEnvelope ping() throws IOException {
        Properties configuration = loadConfiguration();
        String host = configuration.getProperty("chat.server.host", "localhost");
        int port = Integer.parseInt(configuration.getProperty("chat.server.port", "5050"));
        int timeout = (int) Duration.ofSeconds(5).toMillis();
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), timeout);
            s.setSoTimeout(timeout);
            var w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            var r = new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8);
            RequestEnvelope request = new RequestEnvelope(UUID.randomUUID().toString(), ActionType.PING, mapper.createObjectNode());
            w.write(mapper.writeValueAsString(request)); w.newLine(); w.flush();
            String response = JsonLineReader.readLine(r, ProtocolConstants.MAX_MESSAGE_CHARS);
            if (response == null) throw new EOFException("Server disconnected before sending a response");
            ResponseEnvelope envelope = mapper.readValue(response, ResponseEnvelope.class);
            if (!request.requestId().equals(envelope.requestId())) throw new IOException("Response requestId does not match");
            return envelope;
        }
    }

    private Properties loadConfiguration() throws IOException {
        Path path = Path.of(System.getProperty("chat.client.config", "client.properties"));
        Properties properties = new Properties();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
        return properties;
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public synchronized void close() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}

        pendingRequests.forEach((id, future) -> future.completeExceptionally(new IOException("Connection closed")));
        pendingRequests.clear();
    }
}
