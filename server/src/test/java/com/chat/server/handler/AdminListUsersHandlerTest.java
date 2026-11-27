package com.chat.server.handler;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserFilter;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.SortDirection;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.AdminListUsersHandler;
import com.chat.server.model.User;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminListUsersHandlerTest {
    private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
    private AuthHandlerTestSupport.StubUserRepository userRepository;
    private OnlineSessionRegistry sessionRegistry;
    private AdminListUsersHandler handler;
    private ActionDispatcher dispatcher;
    private SessionContext adminSession;
    private SessionContext userSession;

    @BeforeEach
    void setUp() {
        userRepository = AuthHandlerTestSupport.stubUserRepository();
        sessionRegistry = new OnlineSessionRegistry(mapper);
        handler = new AdminListUsersHandler(mapper, userRepository, sessionRegistry);
        dispatcher = new ActionDispatcher(mapper, List.of(handler));

        User admin = AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
        userRepository.addUser(admin);

        User u1 = new User(
                10L, "alice", "hash", "alice@campus.edu", "Alice Wonder", "Addr 1",
                LocalDate.of(2000, 1, 1), Gender.FEMALE, UserRole.USER, AccountStatus.ACTIVE,
                null, null, null,
                OffsetDateTime.parse("2026-11-10T10:00:00+07:00"),
                OffsetDateTime.parse("2026-11-10T10:00:00+07:00")
        );
        User u2 = new User(
                20L, "bob", "hash", "bob@campus.edu", "Bob Builder", "Addr 2",
                LocalDate.of(1999, 2, 2), Gender.MALE, UserRole.USER, AccountStatus.ACTIVE,
                null, null, null,
                OffsetDateTime.parse("2026-11-20T15:30:00+07:00"),
                OffsetDateTime.parse("2026-11-20T15:30:00+07:00")
        );
        User u3 = new User(
                30L, "charlie", "hash", "charlie@campus.edu", "Charlie Chaplin", "Addr 3",
                LocalDate.of(1998, 3, 3), Gender.OTHER, UserRole.USER, AccountStatus.ACTIVE,
                null, null, null,
                OffsetDateTime.parse("2026-10-15T08:00:00+07:00"),
                OffsetDateTime.parse("2026-10-15T08:00:00+07:00")
        );

        userRepository.addUser(u1);
        userRepository.addUser(u2);
        userRepository.addUser(u3);

        adminSession = new SessionContext("conn-admin");
        adminSession.authenticate(1L, "admin", UserRole.ADMIN);

        userSession = new SessionContext("conn-user");
        userSession.authenticate(10L, "alice", UserRole.USER);
    }

    @Test
    void actionReturnsAdminListUsers() {
        assertEquals(ActionType.ADMIN_LIST_USERS, handler.action());
    }

    @Test
    void returnsUsersWithinDateRange() throws Exception {
        DateRange range = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 27));
        AdminUserFilter filter = new AdminUserFilter(
                null, null, range,
                new SortSpec("createdAt", SortDirection.DESC),
                new PageRequest(0, 50));

        RequestEnvelope request = new RequestEnvelope("req-1", ActionType.ADMIN_LIST_USERS, mapper.valueToTree(filter));
        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        assertNull(response.errorCode());

        PageResult<AdminUserInfo> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(2, result.totalItems());
        assertEquals(2, result.items().size());
        assertEquals("bob", result.items().get(0).account().summary().username());
        assertEquals("alice", result.items().get(1).account().summary().username());
    }

    @Test
    void filtersByNameWithinDateRange() throws Exception {
        DateRange range = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 27));
        AdminUserFilter filter = new AdminUserFilter(
                "alice", null, range,
                new SortSpec("name", SortDirection.ASC),
                new PageRequest(0, 50));

        RequestEnvelope request = new RequestEnvelope("req-2", ActionType.ADMIN_LIST_USERS, mapper.valueToTree(filter));
        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        PageResult<AdminUserInfo> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(1, result.totalItems());
        assertEquals("alice", result.items().get(0).account().summary().username());
    }

    @Test
    void rejectsNonAdminUser() {
        AdminUserFilter filter = new AdminUserFilter(
                null, null, null,
                new SortSpec("createdAt", SortDirection.DESC),
                new PageRequest(0, 50));

        RequestEnvelope request = new RequestEnvelope("req-unauth", ActionType.ADMIN_LIST_USERS, mapper.valueToTree(filter));
        ResponseEnvelope response = dispatcher.dispatch(request, userSession);

        assertFalse(response.success());
        assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
    }

    @Test
    void rejectsInvalidSortField() {
        AdminUserFilter filter = new AdminUserFilter(
                null, null, null,
                new SortSpec("passwordHash", SortDirection.DESC),
                new PageRequest(0, 50));

        RequestEnvelope request = new RequestEnvelope("req-sort", ActionType.ADMIN_LIST_USERS, mapper.valueToTree(filter));
        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertFalse(response.success());
        assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    }

    @Test
    void returnsEmptyWhenNoRegistrationsMatch() throws Exception {
        DateRange range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        AdminUserFilter filter = new AdminUserFilter(
                null, null, range,
                new SortSpec("createdAt", SortDirection.DESC),
                new PageRequest(0, 50));

        RequestEnvelope request = new RequestEnvelope("req-empty", ActionType.ADMIN_LIST_USERS, mapper.valueToTree(filter));
        ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

        assertTrue(response.success());
        PageResult<AdminUserInfo> result = mapper.readValue(
                mapper.treeAsTokens(response.data()),
                new TypeReference<>() {});

        assertEquals(0, result.totalItems());
        assertTrue(result.items().isEmpty());
    }
}
