package com.chat.server.socket;

import com.chat.common.protocol.JsonLineReader;
import com.chat.common.protocol.MessageTooLargeException;
import com.chat.common.protocol.ProtocolConstants;
import com.chat.server.handler.RequestHandler;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.chat.server.service.SessionLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class TcpSocketServer implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(TcpSocketServer.class);
    private final int configuredPort;
    private final RequestHandler handler;
    private final OnlineSessionRegistry sessionRegistry;
    private final SessionLifecycle sessionLifecycle;
    private final Set<ClientConnection> connections = ConcurrentHashMap.newKeySet();
    private final ExecutorService acceptor = Executors.newSingleThreadExecutor(r -> new Thread(r, "chat-acceptor"));
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(4, 32, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(256), r -> new Thread(r, "chat-client"), new ThreadPoolExecutor.AbortPolicy());
    private volatile boolean running;
    private ServerSocket serverSocket;

    @Autowired
    public TcpSocketServer(
            @Value("${chat.server.port:5050}") int port,
            RequestHandler handler,
            OnlineSessionRegistry sessionRegistry,
            SessionLifecycle sessionLifecycle) {
        this.configuredPort = port;
        this.handler = handler;
        this.sessionRegistry = sessionRegistry;
        this.sessionLifecycle = sessionLifecycle;
    }

    public TcpSocketServer(int port, RequestHandler handler) {
        this(port, handler, null, null);
    }

    public TcpSocketServer(int port, RequestHandler handler, OnlineSessionRegistry sessionRegistry) {
        this(port, handler, sessionRegistry, null);
    }

    @Override
    public synchronized void start() {
        if (running) return;
        try {
            serverSocket = new ServerSocket(configuredPort);
            running = true;
            acceptor.submit(this::acceptLoop);
            log.info("TCP chat server listening on port {}", getPort());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start TCP server", exception);
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                try {
                    workers.execute(() -> serve(client));
                } catch (RejectedExecutionException exception) {
                    closeSocket(client);
                    log.warn("Rejected connection: server is at capacity");
                }
            } catch (IOException exception) {
                if (running) log.error("Failed to accept TCP client", exception);
            }
        }
    }

    private void serve(Socket client) {
        String connectionId = UUID.randomUUID().toString();
        SessionContext session = new SessionContext(connectionId);
        ClientConnection connection = null;

        try (client;
             Reader reader = new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {

            connection = new ClientConnection(client, writer, session);
            connections.add(connection);
            if (sessionRegistry != null) {
                sessionRegistry.registerConnection(connection);
            }

            String line;
            while (running && (line = readMessage(reader, connection)) != null) {
                String responseJson = handler.handle(line, session);
                connection.send(responseJson);
            }
        } catch (IOException exception) {
            log.debug("Client disconnected: {}", exception.getMessage());
        } finally {
            if (connection != null) {
                connections.remove(connection);
                if (sessionRegistry != null) {
                    sessionRegistry.unregisterConnection(connection);
                }
            }
            if (sessionLifecycle != null && session.isAuthenticated()) {
                sessionLifecycle.end(session);
            }
        }
    }

    private String readMessage(Reader reader, ClientConnection connection) throws IOException {
        try {
            return JsonLineReader.readLine(reader, ProtocolConstants.MAX_MESSAGE_CHARS);
        } catch (MessageTooLargeException exception) {
            connection.send(handler.messageTooLarge());
            return null;
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}

        connections.forEach(ClientConnection::close);
        connections.clear();
        acceptor.shutdownNow();
        workers.shutdownNow();
        boolean interrupted = awaitTermination(acceptor, "acceptor");
        interrupted |= awaitTermination(workers, "worker pool");
        if (interrupted) Thread.currentThread().interrupt();
    }

    private boolean awaitTermination(ExecutorService executor, String name) {
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("TCP {} did not terminate within 5 seconds", name);
            }
            return false;
        } catch (InterruptedException exception) {
            log.warn("Interrupted while waiting for TCP {} to terminate", name);
            return true;
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    public int getPort() {
        return serverSocket == null ? configuredPort : serverSocket.getLocalPort();
    }
}
