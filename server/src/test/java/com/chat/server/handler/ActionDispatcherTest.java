package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.exception.InvalidSortFieldException;
import com.chat.server.handler.impl.PingHandler;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionDispatcherTest {
    private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
    private ActionDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        PingHandler pingHandler = new PingHandler(mapper);
        dispatcher = new ActionDispatcher(mapper, List.of(pingHandler));
    }

    @Test
    void allowsPublicActionForAnonymousSession() {
        SessionContext session = new SessionContext("conn-1");
        RequestEnvelope request = new RequestEnvelope("req-1", ActionType.PING, mapper.createObjectNode());

        ResponseEnvelope response = dispatcher.dispatch(request, session);
        assertTrue(response.success());
        assertEquals("req-1", response.requestId());
        assertEquals("PONG", response.data().get("message").asText());
    }

    @Test
    void blocksProtectedActionWhenUnauthenticated() {
        SessionContext session = new SessionContext("conn-1");
        RequestEnvelope request = new RequestEnvelope("req-2", ActionType.GET_PROFILE, mapper.createObjectNode());

        ResponseEnvelope response = dispatcher.dispatch(request, session);
        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
    }

    @Test
    void blocksAdminActionWhenUserRole() {
        SessionContext session = new SessionContext("conn-1");
        session.authenticate(1L, "regular_user", UserRole.USER);

        RequestEnvelope request = new RequestEnvelope("req-3", ActionType.ADMIN_LIST_USERS, mapper.createObjectNode());
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
    }

    @Test
    void returnsUnsupportedOperationWhenActionHasNoHandler() {
        SessionContext session = new SessionContext("conn-1");
        session.authenticate(2L, "admin_user", UserRole.ADMIN);

        RequestEnvelope request = new RequestEnvelope("req-4", ActionType.ADMIN_LIST_USERS, mapper.createObjectNode());
        ResponseEnvelope response = dispatcher.dispatch(request, session);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNSUPPORTED_OPERATION, response.errorCode());
        assertEquals("Operation not implemented yet", response.message());
    }

    @Test
    void translatesInvalidSortFieldExceptionToValidationFailed() {
        ActionHandler faultyHandler = new ActionHandler() {
            @Override
            public ActionType action() {
                return ActionType.LOGIN;
            }

            @Override
            public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
                throw new InvalidSortFieldException("illegal_field", List.of("name", "createdAt"));
            }
        };

        ActionDispatcher customDispatcher = new ActionDispatcher(mapper, List.of(faultyHandler));
        SessionContext session = new SessionContext("conn-1");
        RequestEnvelope request = new RequestEnvelope("req-5", ActionType.LOGIN, mapper.createObjectNode());

        ResponseEnvelope response = customDispatcher.dispatch(request, session);
        assertFalse(response.success());
        assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    }

    @Test
    void translatesDuplicateKeyExceptionToAlreadyExistsForCreateActions() {
        ResponseEnvelope response = dispatchDuplicateKey(ActionType.REGISTER, new SessionContext("conn-1"));
        assertFalse(response.success());
        assertEquals(ErrorCode.ALREADY_EXISTS, response.errorCode());
        assertEquals("Username or email already exists", response.message());
    }

    @Test
    void translatesDuplicateKeyExceptionToConflictForOtherActions() {
        SessionContext session = new SessionContext("conn-1");
        session.authenticate(1L, "regular_user", UserRole.USER);
        ResponseEnvelope response = dispatchDuplicateKey(ActionType.OPEN_DIRECT_CONVERSATION, session);
        assertFalse(response.success());
        assertEquals(ErrorCode.CONFLICT, response.errorCode());
    }

    @Test
    void translatesIllegalArgumentExceptionToValidationFailed() {
        ActionHandler invalidHandler = new ActionHandler() {
            @Override
            public ActionType action() {
                return ActionType.PING;
            }

            @Override
            public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
                throw new IllegalArgumentException("pageSize must be positive");
            }
        };

        ActionDispatcher customDispatcher = new ActionDispatcher(mapper, List.of(invalidHandler));
        RequestEnvelope request = new RequestEnvelope("req-8", ActionType.PING, mapper.createObjectNode());

        ResponseEnvelope response = customDispatcher.dispatch(request, new SessionContext("conn-1"));
        assertFalse(response.success());
        assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
        assertEquals("pageSize must be positive", response.message());
    }

    private ResponseEnvelope dispatchDuplicateKey(ActionType action, SessionContext session) {
        ActionHandler duplicateHandler = new ActionHandler() {
            @Override
            public ActionType action() {
                return action;
            }

            @Override
            public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
                throw new DuplicateKeyException("duplicate key");
            }
        };

        ActionDispatcher customDispatcher = new ActionDispatcher(mapper, List.of(duplicateHandler));
        RequestEnvelope request = new RequestEnvelope("req-6", action, mapper.createObjectNode());
        return customDispatcher.dispatch(request, session);
    }

    @Test
    void translatesUnexpectedExceptionToInternalError() {
        ActionHandler crashingHandler = new ActionHandler() {
            @Override
            public ActionType action() {
                return ActionType.PING;
            }

            @Override
            public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) {
                throw new RuntimeException("Unexpected database explosion");
            }
        };

        ActionDispatcher customDispatcher = new ActionDispatcher(mapper, List.of(crashingHandler));
        SessionContext session = new SessionContext("conn-1");
        RequestEnvelope request = new RequestEnvelope("req-7", ActionType.PING, mapper.createObjectNode());

        ResponseEnvelope response = customDispatcher.dispatch(request, session);
        assertFalse(response.success());
        assertEquals(ErrorCode.INTERNAL_ERROR, response.errorCode());
        assertEquals("An internal server error occurred", response.message());
    }
}
