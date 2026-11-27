package com.chat.admin.view;

import com.chat.admin.network.SocketClient;
import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.*;

class NewRegistrationPanelTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private ServerSocket serverSocket;
    private SocketClient socketClient;

    @BeforeEach
    void setUp() throws Exception {
        serverSocket = new ServerSocket(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (socketClient != null) socketClient.close();
        if (serverSocket != null) serverSocket.close();
    }

    @Test
    void showsAnErrorAndNoSampleDataWhenDisconnected() throws Exception {
        NewRegistrationPanel panel = onEdt(() -> new NewRegistrationPanel((SocketClient) null));

        assertEquals(0, panel.getTableModel().getRowCount());
        assertEquals("0 accounts", panel.getMatchingRegistrationsLabelText());
        assertEquals("Not connected to the server.", panel.getStatusText());
    }

    @Test
    void showsTheServerErrorAndNoSampleDataWhenTheRequestFails() throws Exception {
        startServer(req -> new ResponseEnvelope(
                req.requestId(), false, null, ErrorCode.INTERNAL_ERROR, "database unavailable"));

        NewRegistrationPanel panel = onEdt(() -> new NewRegistrationPanel(socketClient));

        waitFor(() -> panel.getStatusText().contains("database unavailable"));
        assertEquals(0, panel.getTableModel().getRowCount());
        assertEquals("0 accounts", panel.getMatchingRegistrationsLabelText());
    }

    @Test
    void showsServerRowsInVietnamTimeWithStatusAndActiveTotal() throws Exception {
        startServer(req -> {
            boolean activeOnly = req.data().path("accountStatus").asText("").equals("ACTIVE");
            List<AdminUserInfo> items = List.of(
                    user(101L, "early", AccountStatus.ACTIVE, OffsetDateTime.parse("2026-11-26T20:00:00Z")),
                    user(102L, "gone", AccountStatus.DELETED, OffsetDateTime.parse("2026-11-27T05:00:00Z")));
            PageResult<AdminUserInfo> page = activeOnly
                    ? new PageResult<>(items.subList(0, 1), 0, 1, 61, 61)
                    : new PageResult<>(items, 0, NewRegistrationPanel.PAGE_SIZE, 120, 3);
            return new ResponseEnvelope(req.requestId(), true, mapper.valueToTree(page), null, null);
        });

        NewRegistrationPanel panel = onEdt(() -> new NewRegistrationPanel(socketClient));

        waitFor(() -> panel.getTableModel().getRowCount() == 2
                && panel.getActiveSignupsLabelText().equals("61 active"));
        // 20:00 UTC on the 26th is 03:00 on the 27th in Vietnam.
        assertEquals("2026-11-27 03:00", panel.getTableModel().getValueAt(0, 6));
        assertEquals("Active", panel.getTableModel().getValueAt(0, 7));
        assertEquals("Deleted", panel.getTableModel().getValueAt(1, 7));
        assertEquals("120 accounts", panel.getMatchingRegistrationsLabelText());
        assertEquals("Page 1 of 3", panel.getPageInfoText());
        assertEquals(" ", panel.getStatusText());
    }

    @Test
    void reportsAReversedRangeInsteadOfSwappingIt() throws Exception {
        NewRegistrationPanel panel = onEdt(() -> {
            NewRegistrationPanel p = new NewRegistrationPanel((SocketClient) null);
            p.setDateRange("2026-11-30", "2026-11-01");
            return p;
        });

        assertEquals("The start date must not be after the end date.", panel.getStatusText());
        assertEquals(0, panel.getTableModel().getRowCount());
    }

    @Test
    void formatsRegistrationTimesInVietnamTime() {
        assertEquals("2026-11-27 16:41",
                NewRegistrationPanel.formatRegisteredAt(OffsetDateTime.parse("2026-11-27T09:41:30Z")));
        assertEquals("2026-11-28 06:59",
                NewRegistrationPanel.formatRegisteredAt(OffsetDateTime.parse("2026-11-27T23:59:59Z")));
        assertEquals("2026-11-27 16:41",
                NewRegistrationPanel.formatRegisteredAt(OffsetDateTime.parse("2026-11-27T16:41:00+07:00")));
        assertEquals("N/A", NewRegistrationPanel.formatRegisteredAt(null));
    }

    @Test
    void namesEveryAccountStatus() {
        assertEquals("Active", NewRegistrationPanel.statusText(AccountStatus.ACTIVE));
        assertEquals("Locked", NewRegistrationPanel.statusText(AccountStatus.LOCKED));
        assertEquals("Deleted", NewRegistrationPanel.statusText(AccountStatus.DELETED));
    }

    private AdminUserInfo user(long id, String username, AccountStatus status, OffsetDateTime createdAt) {
        UserSummary summary = new UserSummary(id, username, "Full " + username, status, PresenceStatus.OFFLINE);
        return new AdminUserInfo(new UserAccountDetail(
                summary, username + "@example.test", "Campus 1", LocalDate.of(2001, 1, 1),
                Gender.MALE, UserRole.USER, createdAt, createdAt));
    }

    /** Answers every request on one connection with the given reply. */
    private void startServer(Function<RequestEnvelope, ResponseEnvelope> reply) throws Exception {
        Thread serverThread = new Thread(() -> {
            try (Socket s = serverSocket.accept();
                 BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    RequestEnvelope req = mapper.readValue(line, RequestEnvelope.class);
                    w.write(mapper.writeValueAsString(reply.apply(req)));
                    w.newLine();
                    w.flush();
                }
            } catch (Exception ignored) {
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        socketClient = new SocketClient();
        socketClient.connect("127.0.0.1", serverSocket.getLocalPort());
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        Object[] result = new Object[1];
        Exception[] error = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                result[0] = action.call();
            } catch (Exception e) {
                error[0] = e;
            }
        });
        if (error[0] != null) throw error[0];
        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }

    private static void waitFor(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            boolean[] met = new boolean[1];
            SwingUtilities.invokeAndWait(() -> met[0] = condition.getAsBoolean());
            if (met[0]) return;
            Thread.sleep(25);
        }
        fail("condition not met within 5 seconds");
    }
}
