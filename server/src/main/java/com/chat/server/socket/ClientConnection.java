package com.chat.server.socket;

import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ClientConnection implements AutoCloseable {
    private final Socket socket;
    private final BufferedWriter writer;
    private final SessionContext sessionContext;
    private final Object writeLock = new Object();

    public ClientConnection(Socket socket, BufferedWriter writer, SessionContext sessionContext) {
        this.socket = Objects.requireNonNull(socket, "socket");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext");
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public Socket getSocket() {
        return socket;
    }

    public void send(String jsonLine) throws IOException {
        synchronized (writeLock) {
            writer.write(jsonLine);
            writer.newLine();
            writer.flush();
        }
    }

    public void sendEnvelope(ObjectMapper mapper, Object envelope) throws IOException {
        String json = mapper.writeValueAsString(envelope);
        send(json);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
