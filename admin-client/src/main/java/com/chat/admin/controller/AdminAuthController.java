package com.chat.admin.controller;

import com.chat.admin.network.SocketClient;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.auth.AuthenticationResult;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.social.UserTarget;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller handling authentication and administrator role gating for the Admin Console.
 */
public final class AdminAuthController {
    private static final SocketClient SHARED_SOCKET_CLIENT = new SocketClient();

    private final SocketClient socketClient;
    private final ObjectMapper mapper;

    public record AuthResult(boolean success, AdminUserInfo adminUser, String errorMessage) {}

    public AdminAuthController() {
        this(SHARED_SOCKET_CLIENT);
    }

    public AdminAuthController(SocketClient socketClient) {
        this.socketClient = socketClient;
        this.mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }

    /**
     * Executes asynchronous 2-step admin authentication over TCP socket.
     */
    public CompletableFuture<AuthResult> authenticateAsync(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    new AuthResult(false, null, "Vui lòng nhập tên đăng nhập quản trị viên."));
        }
        if (password == null || password.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    new AuthResult(false, null, "Vui lòng nhập mật khẩu."));
        }

        String cleanUser = username.trim();
        String cleanPass = password.trim();

        try {
            if (!socketClient.isConnected()) {
                socketClient.connect();
            }
        } catch (IOException ex) {
            return CompletableFuture.completedFuture(
                    new AuthResult(false, null, "Không thể kết nối đến máy chủ: " + ex.getMessage()));
        }

        LoginRequest loginReq = new LoginRequest(cleanUser, cleanPass);
        return socketClient.sendAsync(ActionType.LOGIN, loginReq)
                .thenCompose(loginResp -> {
                    if (!loginResp.success()) {
                        String msg = mapErrorCode(loginResp.errorCode(), loginResp.message());
                        return CompletableFuture.completedFuture(new AuthResult(false, null, msg));
                    }

                    AuthenticationResult authResult;
                    try {
                        authResult = mapper.treeToValue(loginResp.data(), AuthenticationResult.class);
                    } catch (Exception e) {
                        return CompletableFuture.completedFuture(
                                new AuthResult(false, null, "Lỗi phân tích kết quả xác thực từ máy chủ."));
                    }

                    long userId = authResult.user().userId();
                    // Step 2: Verify administrative privileges via ADMIN_GET_USER
                    return socketClient.sendAsync(ActionType.ADMIN_GET_USER, new UserTarget(userId))
                            .thenCompose(adminResp -> {
                                if (!adminResp.success()) {
                                    // Non-admin user rejected with UNAUTHORIZED or failure -> auto-logout
                                    return logoutAsync().thenApply(ignored ->
                                            new AuthResult(false, null, "Tài khoản không có quyền quản trị."));
                                }

                                try {
                                    AdminUserInfo adminInfo = mapper.treeToValue(adminResp.data(), AdminUserInfo.class);
                                    if (adminInfo.account().role() != UserRole.ADMIN) {
                                        return logoutAsync().thenApply(ignored ->
                                                new AuthResult(false, null, "Tài khoản không có quyền quản trị."));
                                    }
                                    return CompletableFuture.completedFuture(new AuthResult(true, adminInfo, null));
                                } catch (Exception e) {
                                    return CompletableFuture.completedFuture(
                                            new AuthResult(false, null, "Lỗi phân tích thông tin quyền quản trị."));
                                }
                            });
                })
                .exceptionally(ex -> new AuthResult(false, null, "Không thể kết nối đến máy chủ: " + ex.getMessage()));
    }

    /**
     * Synchronous bridge for legacy or blocking authentication invocations.
     */
    public AuthResult authenticate(String username, String password) {
        try {
            return authenticateAsync(username, password).get(6, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new AuthResult(false, null, "Quá thời gian kết nối hoặc máy chủ không phản hồi.");
        }
    }

    /**
     * Sends a LOGOUT action to clean up server session state.
     */
    public CompletableFuture<ResponseEnvelope> logoutAsync() {
        if (!socketClient.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return socketClient.sendAsync(ActionType.LOGOUT, null);
    }

    private String mapErrorCode(ErrorCode code, String fallbackMessage) {
        if (code == null) {
            return fallbackMessage != null ? fallbackMessage : "Đăng nhập thất bại.";
        }
        return switch (code) {
            case INVALID_CREDENTIALS -> "Tên đăng nhập hoặc mật khẩu không chính xác.";
            case ACCOUNT_LOCKED -> "Tài khoản đã bị khóa bởi quản trị viên hệ thống.";
            case ACCOUNT_DELETED -> "Tài khoản đã bị vô hiệu hóa hoặc xóa.";
            case UNAUTHORIZED -> "Tài khoản không có quyền quản trị.";
            case UNAUTHENTICATED -> "Phiên làm việc chưa được xác thực.";
            default -> (fallbackMessage != null && !fallbackMessage.isBlank())
                    ? fallbackMessage
                    : "Đăng nhập thất bại (Mã lỗi: " + code + ").";
        };
    }
}
