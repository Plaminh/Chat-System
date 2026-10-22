package com.chat.server.socket;

import com.chat.common.protocol.JsonLineReader;
import com.chat.common.protocol.MessageTooLargeException;
import com.chat.common.protocol.ProtocolConstants;
import com.chat.server.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class TcpSocketServer implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(TcpSocketServer.class);
    private final int configuredPort;
    private final RequestHandler handler;
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService acceptor = Executors.newSingleThreadExecutor(r -> new Thread(r, "chat-acceptor"));
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(4, 32, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(256), r -> new Thread(r, "chat-client"), new ThreadPoolExecutor.AbortPolicy());
    private volatile boolean running;
    private ServerSocket serverSocket;

    public TcpSocketServer(@Value("${chat.server.port:5050}") int port, RequestHandler handler) { this.configuredPort = port; this.handler = handler; }

    @Override public synchronized void start() {
        if (running) return;
        try { serverSocket = new ServerSocket(configuredPort); running = true; acceptor.submit(this::acceptLoop); log.info("TCP chat server listening on port {}", getPort()); }
        catch (IOException exception) { throw new IllegalStateException("Could not start TCP server", exception); }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept(); clients.add(client);
                try { workers.execute(() -> serve(client)); }
                catch (RejectedExecutionException exception) { closeClient(client); log.warn("Rejected connection: server is at capacity"); }
            } catch (IOException exception) { if (running) log.error("Failed to accept TCP client", exception); }
        }
    }

    private void serve(Socket client) {
        try (client;
             Reader reader = new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = readMessage(reader, writer)) != null) {
                writer.write(handler.handle(line)); writer.newLine(); writer.flush();
            }
        } catch (IOException exception) { log.debug("Client disconnected: {}", exception.getMessage()); }
        finally { clients.remove(client); }
    }

    private String readMessage(Reader reader, BufferedWriter writer) throws IOException {
        try { return JsonLineReader.readLine(reader, ProtocolConstants.MAX_MESSAGE_CHARS); }
        catch (MessageTooLargeException exception) { writer.write(handler.messageTooLarge()); writer.newLine(); writer.flush(); return null; }
    }

    @Override public synchronized void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) { }
        clients.forEach(this::closeClient); clients.clear();
        acceptor.shutdownNow(); workers.shutdownNow();
        boolean interrupted = awaitTermination(acceptor, "acceptor");
        interrupted |= awaitTermination(workers, "worker pool");
        if (interrupted) Thread.currentThread().interrupt();
    }
    private boolean awaitTermination(ExecutorService executor, String name) {
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) log.warn("TCP {} did not terminate within 5 seconds", name);
            return false;
        } catch (InterruptedException exception) {
            log.warn("Interrupted while waiting for TCP {} to terminate", name);
            return true;
        }
    }
    private void closeClient(Socket socket) { try { socket.close(); } catch (IOException ignored) { } }
    @Override public boolean isRunning() { return running; }
    @Override public int getPhase() { return Integer.MAX_VALUE; }
    public int getPort() { return serverSocket == null ? configuredPort : serverSocket.getLocalPort(); }
}
