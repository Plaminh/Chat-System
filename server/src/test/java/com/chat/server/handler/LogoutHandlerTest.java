package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.LogoutHandler;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogoutHandlerTest {
    private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
    private AuthHandlerTestSupport.StubLoginHistoryRepository loginHistoryRepository;
    private OnlineSessionRegistry sessionRegistry;
    private LogoutHandler handler;

    @BeforeEach
    void setUp() {
        loginHistoryRepository = AuthHandlerTestSupport.stubLoginHistoryRepository();
        sessionRegistry = new OnlineSessionRegistry(mapper);
        handler = new LogoutHandler(loginHistoryRepository, sessionRegistry);
    }

    @Test
    void actionIsLogout() {
        assertEquals(ActionType.LOGOUT, handler.action());
    }

    @Test
    void clearsSessionAndRecordsLogoutTimestamp() {
        SessionContext session = new SessionContext("conn-logout-1");
        session.authenticate(1L, "admin", UserRole.ADMIN, 999L);
        assertTrue(session.isAuthenticated());

        RequestEnvelope request = new RequestEnvelope("req-logout-1", ActionType.LOGOUT, mapper.createObjectNode());
        ResponseEnvelope response = handler.handle(request, session);

        assertTrue(response.success());
        assertEquals("req-logout-1", response.requestId());
        assertNull(response.data());

        assertFalse(session.isAuthenticated());
        assertNull(session.getUserId());
        assertNull(session.getRole());
        assertNull(session.getLoginHistoryId());

        assertTrue(loginHistoryRepository.isLoggedOut(999L));
    }
}
