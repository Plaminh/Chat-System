package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.AuthenticationResult;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.LoginHandler;
import com.chat.server.model.User;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginHandlerTest {
    private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
    private AuthHandlerTestSupport.StubUserRepository userRepository;
    private AuthHandlerTestSupport.StubLoginHistoryRepository loginHistoryRepository;
    private LoginHandler handler;

    @BeforeEach
    void setUp() {
        userRepository = AuthHandlerTestSupport.stubUserRepository();
        loginHistoryRepository = AuthHandlerTestSupport.stubLoginHistoryRepository();
        handler = new LoginHandler(mapper, userRepository, loginHistoryRepository);
    }

    @Test
    void actionIsLogin() {
        assertEquals(ActionType.LOGIN, handler.action());
    }

    @Test
    void authenticatesAdminSuccessfully() throws Exception {
        User admin = AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
        userRepository.addUser(admin);

        LoginRequest req = new LoginRequest("admin", AuthHandlerTestSupport.DEFAULT_RAW_PASSWORD);
        RequestEnvelope envelope = new RequestEnvelope("req-login-1", ActionType.LOGIN, mapper.valueToTree(req));
        SessionContext session = new SessionContext("conn-1");

        ResponseEnvelope response = handler.handle(envelope, session);

        assertTrue(response.success());
        assertEquals("req-login-1", response.requestId());
        assertNotNull(response.data());

        AuthenticationResult result = mapper.treeToValue(response.data(), AuthenticationResult.class);
        assertEquals(1L, result.user().userId());
        assertEquals("admin", result.user().username());

        assertTrue(session.isAuthenticated());
        assertEquals(1L, session.getUserId());
        assertEquals(UserRole.ADMIN, session.getRole());
        assertNotNull(session.getLoginHistoryId());

        assertEquals(1L, loginHistoryRepository.getLoginUserId(session.getLoginHistoryId()));
    }

    @Test
    void rejectsInvalidPassword() throws Exception {
        User user = AuthHandlerTestSupport.createTestUser(2L, "user2", UserRole.USER, AccountStatus.ACTIVE);
        userRepository.addUser(user);

        LoginRequest req = new LoginRequest("user2", "WrongPassword123!");
        RequestEnvelope envelope = new RequestEnvelope("req-login-2", ActionType.LOGIN, mapper.valueToTree(req));
        SessionContext session = new SessionContext("conn-2");

        ResponseEnvelope response = handler.handle(envelope, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.INVALID_CREDENTIALS, response.errorCode());
        assertFalse(session.isAuthenticated());
    }

    @Test
    void rejectsLockedAccount() throws Exception {
        User lockedUser = AuthHandlerTestSupport.createTestUser(3L, "locked", UserRole.USER, AccountStatus.LOCKED);
        userRepository.addUser(lockedUser);

        LoginRequest req = new LoginRequest("locked", AuthHandlerTestSupport.DEFAULT_RAW_PASSWORD);
        RequestEnvelope envelope = new RequestEnvelope("req-login-3", ActionType.LOGIN, mapper.valueToTree(req));
        SessionContext session = new SessionContext("conn-3");

        ResponseEnvelope response = handler.handle(envelope, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.ACCOUNT_LOCKED, response.errorCode());
        assertFalse(session.isAuthenticated());
    }

    @Test
    void rejectsDeletedAccount() throws Exception {
        User deletedUser = AuthHandlerTestSupport.createTestUser(4L, "deleted", UserRole.USER, AccountStatus.DELETED);
        userRepository.addUser(deletedUser);

        LoginRequest req = new LoginRequest("deleted", AuthHandlerTestSupport.DEFAULT_RAW_PASSWORD);
        RequestEnvelope envelope = new RequestEnvelope("req-login-4", ActionType.LOGIN, mapper.valueToTree(req));
        SessionContext session = new SessionContext("conn-4");

        ResponseEnvelope response = handler.handle(envelope, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.ACCOUNT_DELETED, response.errorCode());
        assertFalse(session.isAuthenticated());
    }

    @Test
    void rejectsNonExistentUserWithInvalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest("ghost", "any_password");
        RequestEnvelope envelope = new RequestEnvelope("req-login-5", ActionType.LOGIN, mapper.valueToTree(req));
        SessionContext session = new SessionContext("conn-5");

        ResponseEnvelope response = handler.handle(envelope, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.INVALID_CREDENTIALS, response.errorCode());
        assertFalse(session.isAuthenticated());
    }
}
