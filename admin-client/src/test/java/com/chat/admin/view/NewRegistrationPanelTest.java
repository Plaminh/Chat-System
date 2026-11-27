package com.chat.admin.view;

import com.chat.admin.network.SocketClient;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NewRegistrationPanelTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private ServerSocket serverSocket;
    private SocketClient socketClient;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
        socketClient = new SocketClient();
        socketClient.connect("127.0.0.1", serverSocket.getLocalPort());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (socketClient != null) socketClient.close();
        if (serverSocket != null) serverSocket.close();
    }

    @Test
    void instantiatesAndPopulatesMockWhenDisconnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skip GUI test in headless mode");

        NewRegistrationPanel panel = new NewRegistrationPanel((SocketClient) null);
        assertNotNull(panel);
        assertTrue(panel.getTableModel().getRowCount() > 0);
        assertNotNull(panel.getMatchingRegistrationsLabelText());
    }

    @Test
    void populatesFromSocketClientWhenConnected() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skip GUI test in headless mode");

        CountDownLatch requestReceived = new CountDownLatch(1);

        Thread serverThread = new Thread(() -> {
            try (Socket s = serverSocket.accept();
                 BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {

                String line = r.readLine();
                if (line != null) {
                    RequestEnvelope req = mapper.readValue(line, RequestEnvelope.class);

                    UserSummary summary = new UserSummary(101L, "newuser", "New User", AccountStatus.ACTIVE, PresenceStatus.ONLINE);
                    UserAccountDetail detail = new UserAccountDetail(
                            summary, "newuser@campus.edu", "Campus 1",
                            LocalDate.of(2001, 1, 1), Gender.MALE, UserRole.USER,
                            OffsetDateTime.now(), OffsetDateTime.now()
                    );
                    AdminUserInfo info = new AdminUserInfo(detail);
                    PageResult<AdminUserInfo> pageResult = new PageResult<>(List.of(info), 0, 50, 1, 1);

                    ResponseEnvelope resp = new ResponseEnvelope(req.requestId(), true, mapper.valueToTree(pageResult), null, null);
                    w.write(mapper.writeValueAsString(resp));
                    w.newLine();
                    w.flush();
                    requestReceived.countDown();
                }
            } catch (Exception ignored) {}
        });
        serverThread.setDaemon(true);
        serverThread.start();

        NewRegistrationPanel panel = new NewRegistrationPanel(socketClient);
        assertNotNull(panel);
        assertTrue(requestReceived.await(3, TimeUnit.SECONDS));
    }
}
