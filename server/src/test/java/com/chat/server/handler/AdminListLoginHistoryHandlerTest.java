package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.LoginHistoryEntry;
import com.chat.common.dto.admin.LoginHistoryRequest;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.SortDirection;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.AdminListLoginHistoryHandler;
import com.chat.server.model.LoginHistoryRecord;
import com.chat.server.model.User;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminListLoginHistoryHandlerTest {
    private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
    private AuthHandlerTestSupport.StubUserRepository userRepository;
    private AuthHandlerTestSupport.StubLoginHistoryRepository loginHistoryRepository;
    private AdminListLoginHistoryHandler handler;
    private ActionDispatcher dispatcher;
    private SessionContext adminSession;

    @BeforeEach
    void setUp() {
        userRepository = AuthHandlerTestSupport.stubUserRepository();
        loginHistoryRepository = AuthHandlerTestSupport.stubLoginHistoryRepository();
        handler = new AdminListLoginHistoryHandler(mapper, loginHistoryRepository, userRepository);
        dispatcher = new ActionDispatcher(mapper, List.of(handler));

        User admin = AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
        User user2 = AuthHandlerTestSupport.createTestUser(2L, "user2", UserRole.USER, AccountStatus.ACTIVE);
        userRepository.addUser(admin);
        userRepository.addUser(user2);

        adminSession = new SessionContext("conn-admin");
        adminSession.authenticate(1L, "admin", UserRole.ADMIN);

        loginHistoryRepository.addRecord(new LoginHistoryRecord(
                101L, 1L, "admin", "Full Name admin",
                OffsetDateTime.now().minusHours(2), OffsetDateTime.now().minusHours(1)));
        loginHistoryRepository.addRecord(new LoginHistoryRecord(
                102L, 2L, "user2", "Full Name user2",
                OffsetDateTime.now().minusMinutes(30), null));
    }

    @Test
    void actionReturnsAdminListLoginHistory() {
        assertEquals(ActionType.ADMIN_LIST_LOGIN_HISTORY, handler.action());
    }

    @Test
    void returnsGlobalLoginHistoryChronologically() throws Exception {
        LoginHistoryRequest req = new LoginHistoryRequest(
                null,
                new SortSpec("loggedInAt", SortDirection.DESC),
                new PageRequest(0, 10));

        RequestEnvelope request = new RequestEnvelope(
                "req-hist-1",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.valueToTree(req));

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        assertNull(response.errorCode());

        PageResult<LoginHistoryEntry> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(2, result.totalItems());
        assertEquals(2, result.items().size());
        assertEquals("user2", result.items().get(0).username());
        assertEquals("admin", result.items().get(1).username());
    }

    @Test
    void returnsLoginHistoryAscendingOrder() throws Exception {
        LoginHistoryRequest req = new LoginHistoryRequest(
                null,
                new SortSpec("loggedInAt", SortDirection.ASC),
                new PageRequest(0, 10));

        RequestEnvelope request = new RequestEnvelope(
                "req-hist-6",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.valueToTree(req));

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        PageResult<LoginHistoryEntry> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(2, result.items().size());
        assertEquals("admin", result.items().get(0).username());
        assertEquals("user2", result.items().get(1).username());
    }

    @Test
    void returnsFilteredLoginHistoryForSpecificUser() throws Exception {
        LoginHistoryRequest req = new LoginHistoryRequest(
                2L,
                new SortSpec("loggedInAt", SortDirection.DESC),
                new PageRequest(0, 10));

        RequestEnvelope request = new RequestEnvelope(
                "req-hist-2",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.valueToTree(req));

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        PageResult<LoginHistoryEntry> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(1, result.totalItems());
        assertEquals(1, result.items().size());
        assertEquals(2L, result.items().get(0).userId());
        assertEquals("user2", result.items().get(0).username());
    }

    @Test
    void returnsNotFoundWhenTargetUserDoesNotExist() {
        LoginHistoryRequest req = new LoginHistoryRequest(
                999L,
                new SortSpec("loggedInAt", SortDirection.DESC),
                new PageRequest(0, 10));

        RequestEnvelope request = new RequestEnvelope(
                "req-hist-3",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.valueToTree(req));

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertFalse(response.success());
        assertEquals(ErrorCode.NOT_FOUND, response.errorCode());
        assertEquals("User not found", response.message());
    }

    @Test
    void rejectsInvalidSortFieldWithValidationFailed() throws Exception {
        RequestEnvelope request = new RequestEnvelope(
                "req-hist-4",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.readTree("{\"userId\":null,\"sort\":{\"field\":\"invalidField\",\"direction\":\"DESC\"},\"page\":{\"page\":0,\"size\":10}}"));

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertFalse(response.success());
        assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    }

    @Test
    void rejectsEmptyPayloadWithValidationFailed() {
        RequestEnvelope request = new RequestEnvelope(
                "req-hist-5",
                ActionType.ADMIN_LIST_LOGIN_HISTORY,
                mapper.createObjectNode());

        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertFalse(response.success());
        assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    }
}
