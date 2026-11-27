package com.chat.admin.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.chat.admin.network.SocketClient;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminAuthControllerTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private ServerSocket serverSocket;
  private SocketClient socketClient;
  private AdminAuthController controller;
  private Thread serverThread;

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
    if (serverThread != null) serverThread.join(2000);
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
  void autoLogoutsAndRejectsWhenAdminCheckReturnsUnauthorized() {
    List<JsonNode> received =
        startFakeServer(
            request ->
                switch (action(request)) {
                  case "LOGIN" -> success(request, loginResult(55L, "user55"));
                  case "ADMIN_GET_USER" ->
                      failure(request, ErrorCode.UNAUTHORIZED, "Admin privileges required");
                  default -> success(request, mapper.createObjectNode());
                });

    AdminAuthController.AuthResult result =
        controller.authenticateAsync("user55", "userpass").join();

    assertFalse(result.success());
    assertEquals(AdminAuthController.NOT_ADMIN_MESSAGE, result.errorMessage());
    assertEquals(
        List.of("LOGIN", "ADMIN_GET_USER", "LOGOUT"),
        actions(received),
        "Controller should dispatch LOGOUT to server upon receiving non-admin rejection");
    assertEquals(55L, received.get(1).get("data").get("targetUserId").asLong());
  }

  @Test
  void acceptsAdminAccountWithoutLoggingOut() {
    List<JsonNode> received =
        startFakeServer(
            request ->
                switch (action(request)) {
                  case "LOGIN" -> success(request, loginResult(1L, "admin_dev"));
                  case "ADMIN_GET_USER" ->
                      success(request, adminUserInfo(1L, "admin_dev", "ADMIN"));
                  default -> success(request, mapper.createObjectNode());
                });

    AdminAuthController.AuthResult result =
        controller.authenticateAsync("admin_dev", "secret").join();

    assertTrue(result.success(), result.errorMessage());
    assertEquals("admin_dev", result.adminUser().account().summary().username());
    assertEquals(List.of("LOGIN", "ADMIN_GET_USER"), actions(received));
  }

  @Test
  void reportsRealErrorAndLogsOutWhenAdminCheckFailsForOtherReason() {
    List<JsonNode> received =
        startFakeServer(
            request ->
                switch (action(request)) {
                  case "LOGIN" -> success(request, loginResult(1L, "admin_dev"));
                  case "ADMIN_GET_USER" ->
                      failure(
                          request, ErrorCode.INTERNAL_ERROR, "An internal server error occurred");
                  default -> success(request, mapper.createObjectNode());
                });

    AdminAuthController.AuthResult result =
        controller.authenticateAsync("admin_dev", "secret").join();

    assertFalse(result.success());
    assertNotEquals(AdminAuthController.NOT_ADMIN_MESSAGE, result.errorMessage());
    assertTrue(
        result.errorMessage().contains("An internal server error occurred"), result.errorMessage());
    assertEquals(List.of("LOGIN", "ADMIN_GET_USER", "LOGOUT"), actions(received));
  }

  @Test
  void logsOutWhenAdminInfoCannotBeParsed() {
    List<JsonNode> received =
        startFakeServer(
            request ->
                switch (action(request)) {
                  case "LOGIN" -> success(request, loginResult(1L, "admin_dev"));
                  case "ADMIN_GET_USER" ->
                      success(request, mapper.createObjectNode().put("account", "garbage"));
                  default -> success(request, mapper.createObjectNode());
                });

    AdminAuthController.AuthResult result =
        controller.authenticateAsync("admin_dev", "secret").join();

    assertFalse(result.success());
    assertEquals(List.of("LOGIN", "ADMIN_GET_USER", "LOGOUT"), actions(received));
  }

  @Test
  void sendsPasswordExactlyAsTyped() {
    List<JsonNode> received =
        startFakeServer(request -> failure(request, ErrorCode.INVALID_CREDENTIALS, "Invalid"));

    AdminAuthController.AuthResult result =
        controller.authenticateAsync("  admin_dev  ", " pass word ").join();

    assertFalse(result.success());
    JsonNode loginData = received.get(0).get("data");
    assertEquals("admin_dev", loginData.get("username").asText());
    assertEquals(" pass word ", loginData.get("password").asText());
  }

  /** Accepts one connection and answers each request line with {@code responder} until EOF. */
  private List<JsonNode> startFakeServer(Function<JsonNode, ResponseEnvelope> responder) {
    List<JsonNode> received = new CopyOnWriteArrayList<>();
    serverThread =
        new Thread(
            () -> {
              try (Socket s = serverSocket.accept();
                  BufferedReader r =
                      new BufferedReader(
                          new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                  BufferedWriter w =
                      new BufferedWriter(
                          new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                  JsonNode request = mapper.readTree(line);
                  received.add(request);
                  w.write(mapper.writeValueAsString(responder.apply(request)));
                  w.newLine();
                  w.flush();
                }
              } catch (Exception ignored) {
              }
            });
    serverThread.setDaemon(true);
    serverThread.start();
    return received;
  }

  private static String action(JsonNode request) {
    return request.get("action").asText();
  }

  private static List<String> actions(List<JsonNode> requests) {
    return requests.stream().map(AdminAuthControllerTest::action).toList();
  }

  private static ResponseEnvelope success(JsonNode request, JsonNode data) {
    return new ResponseEnvelope(request.get("requestId").asText(), true, data, null, null);
  }

  private static ResponseEnvelope failure(JsonNode request, ErrorCode code, String message) {
    return new ResponseEnvelope(request.get("requestId").asText(), false, null, code, message);
  }

  private ObjectNode summary(long userId, String username) {
    return mapper
        .createObjectNode()
        .put("userId", userId)
        .put("username", username)
        .put("fullName", "Full " + username)
        .put("accountStatus", "ACTIVE")
        .put("presence", "ONLINE");
  }

  private ObjectNode loginResult(long userId, String username) {
    ObjectNode result = mapper.createObjectNode();
    result.set("user", summary(userId, username));
    result.put("authenticatedAt", "2026-11-23T10:00:00Z");
    return result;
  }

  private ObjectNode adminUserInfo(long userId, String username, String role) {
    ObjectNode account = mapper.createObjectNode();
    account.set("summary", summary(userId, username));
    account
        .put("email", username + "@example.test")
        .put("address", "Fictional District 1")
        .put("dateOfBirth", "1988-01-15")
        .put("gender", "OTHER")
        .put("role", role)
        .put("createdAt", "2026-01-01T01:00:00Z")
        .put("updatedAt", "2026-01-01T01:00:00Z");
    ObjectNode info = mapper.createObjectNode();
    info.set("account", account);
    return info;
  }
}
