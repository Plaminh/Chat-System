package com.chat.server.socket;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.server.handler.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TcpSocketServerTest {
    @Test void servesPingOverLocalhost() throws Exception {
        ObjectMapper mapper = new ObjectMapper(); TcpSocketServer server = new TcpSocketServer(0, new RequestHandler(mapper)); server.start();
        try (Socket socket = new Socket("127.0.0.1", server.getPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer.write("{\"requestId\":\"tcp-1\",\"action\":\"PING\",\"data\":{}}\n"); writer.flush();
            ResponseEnvelope response = mapper.readValue(reader.readLine(), ResponseEnvelope.class);
            assertTrue(response.success()); assertEquals("PONG", response.data().get("message").asText());
        } finally { server.stop(); }
    }
}
