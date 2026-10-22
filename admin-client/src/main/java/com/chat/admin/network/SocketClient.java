package com.chat.admin.network;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.protocol.JsonLineReader;
import com.chat.common.protocol.ProtocolConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public final class SocketClient {
    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEnvelope ping() throws IOException {
        Properties configuration = loadConfiguration();
        String host = configuration.getProperty("chat.server.host", "localhost");
        int port = Integer.parseInt(configuration.getProperty("chat.server.port", "5050"));
        int timeout = (int) Duration.ofSeconds(5).toMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);
            var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            var reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
            RequestEnvelope request = new RequestEnvelope(UUID.randomUUID().toString(), ActionType.PING, mapper.createObjectNode());
            writer.write(mapper.writeValueAsString(request)); writer.newLine(); writer.flush();
            String response = JsonLineReader.readLine(reader, ProtocolConstants.MAX_MESSAGE_CHARS);
            if (response == null) throw new EOFException("Server disconnected before sending a response");
            ResponseEnvelope envelope = mapper.readValue(response, ResponseEnvelope.class);
            if (!request.requestId().equals(envelope.requestId())) throw new IOException("Response requestId does not match");
            return envelope;
        }
    }

    private Properties loadConfiguration() throws IOException {
        Path path = Path.of(System.getProperty("chat.client.config", "client.properties"));
        Properties properties = new Properties();
        if (Files.isRegularFile(path)) try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { properties.load(reader); }
        return properties;
    }
}
