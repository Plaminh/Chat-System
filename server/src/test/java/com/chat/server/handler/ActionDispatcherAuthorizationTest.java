package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.social.UserTarget;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.AdminGetUserHandler;
import com.chat.server.model.User;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionDispatcherAuthorizationTest {
    private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
    private AuthHandlerTestSupport.StubUserRepository userRepository;
    private OnlineSessionRegistry sessionRegistry;
    private ActionDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        userRepository = AuthHandlerTestSupport.stubUserRepository();
        sessionRegistry = new OnlineSessionRegistry(mapper);
        AdminGetUserHandler getUserHandler = new AdminGetUserHandler(mapper, userRepository, sessionRegistry);
        dispatcher = new ActionDispatcher(mapper, List.of(getUserHandler));
    }

    @Test
    void rejectsAdminGetUserWhenUserRoleIsNotAdmin() {
        SessionContext session = new SessionContext("conn-user-1");
        session.authenticate(2L, "user2", UserRole.USER);

        RequestEnvelope request = new RequestEnvelope("req-sec-1", ActionType.ADMIN_GET_USER, mapper.valueToTree(new UserTarget(2L)));
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
        assertEquals("Admin privileges required", response.message());
    }

    @Test
    void rejectsAdminGetUserWhenUnauthenticated() {
        SessionContext session = new SessionContext("conn-anon-1");

        RequestEnvelope request = new RequestEnvelope("req-sec-2", ActionType.ADMIN_GET_USER, mapper.valueToTree(new UserTarget(1L)));
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
        assertEquals("Authentication required", response.message());
    }

    @Test
    void allowsAdminGetUserWhenSessionHasAdminRole() {
        User admin = AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
        userRepository.addUser(admin);

        SessionContext session = new SessionContext("conn-admin-1");
        session.authenticate(1L, "admin", UserRole.ADMIN);

        RequestEnvelope request = new RequestEnvelope("req-sec-3", ActionType.ADMIN_GET_USER, mapper.valueToTree(new UserTarget(1L)));
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertTrue(response.success());
        assertNotNull(response.data());
    }

    @Test
    void rejectsAdminListLoginHistoryWhenUserRoleIsNotAdmin() {
        SessionContext session = new SessionContext("conn-user-2");
        session.authenticate(2L, "user2", UserRole.USER);

        RequestEnvelope request = new RequestEnvelope("req-sec-4", ActionType.ADMIN_LIST_LOGIN_HISTORY, mapper.createObjectNode());
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
        assertEquals("Admin privileges required", response.message());
    }

    @Test
    void rejectsAdminListLoginHistoryWhenUnauthenticated() {
        SessionContext session = new SessionContext("conn-anon-2");

        RequestEnvelope request = new RequestEnvelope("req-sec-5", ActionType.ADMIN_LIST_LOGIN_HISTORY, mapper.createObjectNode());
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
        assertEquals("Authentication required", response.message());
    }
}
