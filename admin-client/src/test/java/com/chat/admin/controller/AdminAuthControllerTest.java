package com.chat.admin.controller;

import com.chat.admin.network.SocketClient;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AdminAuthControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private ServerSocket serverSocket;
    private SocketClient socketClient;
    private AdminAuthController controller;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
        socketClient = new SocketClient();
        socketClient.connect("127.0.0.1", serverSocket.getLocalPort());
        controller = new AdminAuthController(socketClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (socketClient != null) socketClient.close();
        if (serverSocket != null) serverSocket.close();
    }

    @Test
    void rejectsEmptyUsernameOrPasswordImmediately() {
        AdminAuthController.AuthResult res1 = controller.authenticateAsync("", "pass").join();
        assertFalse(res1.success());
        assertTrue(res1.errorMessage().contains("tên đăng nhập"));

        AdminAuthController.AuthResult res2 = controller.authenticateAsync("admin", "").join();
        assertFalse(res2.success());
        assertTrue(res2.errorMessage().contains("mật khẩu"));
    }

    @Test
    void autoLogoutsAndRejectsWhenAdminCheckReturnsUnauthorized() throws Exception {
        AtomicBoolean logoutReceived = new AtomicBoolean(false);

        Thread serverThread = new Thread(() -> {
            try (Socket s = serverSocket.accept();
                 BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {

                // 1. Receive LOGIN request
                String loginLine = r.readLine();
                var loginReq = mapper.readTree(loginLine);
                String loginReqId = loginReq.get("requestId").asText();

                // Respond LOGIN success for a user
                var authUser = mapper.createObjectNode()
                        .put("userId", 55L)
                        .put("username", "user55")
                        .put("fullName", "User 55")
                        .put("accountStatus", "ACTIVE")
                        .put("presence", "ONLINE");
                var authResultNode = mapper.createObjectNode();
                authResultNode.set("user", authUser);
                authResultNode.put("authenticatedAt", "2026-11-23T10:00:00Z");

                ResponseEnvelope loginResp = new ResponseEnvelope(loginReqId, true, authResultNode, null, null);
                w.write(mapper.writeValueAsString(loginResp));
                w.newLine();
                w.flush();

                // 2. Receive ADMIN_GET_USER request
                String adminCheckLine = r.readLine();
                var adminReq = mapper.readTree(adminCheckLine);
                String adminReqId = adminReq.get("requestId").asText();

                // Reject with UNAUTHORIZED
                ResponseEnvelope adminResp = new ResponseEnvelope(adminReqId, false, null, ErrorCode.UNAUTHORIZED, "Admin privileges required");
                w.write(mapper.writeValueAsString(adminResp));
                w.newLine();
                w.flush();

                // 3. Client MUST immediately send LOGOUT
                String logoutLine = r.readLine();
                if (logoutLine != null) {
                    var logoutReq = mapper.readTree(logoutLine);
                    if ("LOGOUT".equals(logoutReq.get("action").asText())) {
                        logoutReceived.set(true);
                        String logoutReqId = logoutReq.get("requestId").asText();
                        ResponseEnvelope logoutResp = new ResponseEnvelope(logoutReqId, true, null, null, null);
                        w.write(mapper.writeValueAsString(logoutResp));
                        w.newLine();
                        w.flush();
                    }
                }
            } catch (Exception ignored) {}
        });
        serverThread.start();

        AdminAuthController.AuthResult result = controller.authenticateAsync("user55", "userpass").join();

        assertFalse(result.success());
        assertEquals("Tài khoản không có quyền quản trị.", result.errorMessage());
        assertTrue(logoutReceived.get(), "Controller should dispatch LOGOUT to server upon receiving non-admin rejection");
    }
}
