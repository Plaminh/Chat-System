package com.chat.server.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.common.YearRequest;
import com.chat.common.dto.statistics.MonthlyCount;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.AdminGetMonthlyRegistrationsHandler;
import com.chat.server.model.User;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminGetMonthlyRegistrationsHandlerTest {
  private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
  private AuthHandlerTestSupport.StubUserRepository userRepository;
  private AdminGetMonthlyRegistrationsHandler handler;
  private ActionDispatcher dispatcher;
  private SessionContext adminSession;
  private SessionContext userSession;

  @BeforeEach
  void setUp() {
    userRepository = AuthHandlerTestSupport.stubUserRepository();
    handler = new AdminGetMonthlyRegistrationsHandler(mapper, userRepository);
    dispatcher = new ActionDispatcher(mapper, List.of(handler));

    User admin =
        AuthHandlerTestSupport.createTestUser(1L, "admin", UserRole.ADMIN, AccountStatus.ACTIVE);
    userRepository.addUser(admin);

    // Add users registered in 2026 across different months
    userRepository.addUser(
        new User(
            10L, "u10", "h", "u10@campus.edu", "User 10", "Addr",
            LocalDate.of(2000, 1, 1), Gender.MALE, UserRole.USER, AccountStatus.ACTIVE,
            null, null, null,
            OffsetDateTime.parse("2026-01-15T10:00:00+07:00"),
            OffsetDateTime.parse("2026-01-15T10:00:00+07:00")));

    userRepository.addUser(
        new User(
            11L, "u11", "h", "u11@campus.edu", "User 11", "Addr",
            LocalDate.of(2000, 1, 1), Gender.FEMALE, UserRole.USER, AccountStatus.ACTIVE,
            null, null, null,
            OffsetDateTime.parse("2026-01-20T10:00:00+07:00"),
            OffsetDateTime.parse("2026-01-20T10:00:00+07:00")));

    userRepository.addUser(
        new User(
            12L, "u12", "h", "u12@campus.edu", "User 12", "Addr",
            LocalDate.of(2000, 1, 1), Gender.MALE, UserRole.USER, AccountStatus.ACTIVE,
            null, null, null,
            OffsetDateTime.parse("2026-05-10T10:00:00+07:00"),
            OffsetDateTime.parse("2026-05-10T10:00:00+07:00")));

    adminSession = new SessionContext("conn-admin");
    adminSession.authenticate(1L, "admin", UserRole.ADMIN);

    userSession = new SessionContext("conn-user");
    userSession.authenticate(2L, "normalUser", UserRole.USER);
  }

  @Test
  void testReturnsTwelveMonthsForValidYear() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-chart-1",
            ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS,
            mapper.valueToTree(new YearRequest(2026)));

    ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

    assertTrue(response.success());
    assertNull(response.errorCode());

    List<MonthlyCount> list =
        mapper.convertValue(response.data(), new TypeReference<List<MonthlyCount>>() {});

    assertNotNull(list);
    assertEquals(12, list.size());

    // Month 1 should have 2 registrations
    assertEquals(1, list.get(0).month());
    assertEquals(2026, list.get(0).year());
    assertEquals(2, list.get(0).count());

    // Month 2, 3, 4 should have 0
    assertEquals(2, list.get(1).month());
    assertEquals(0, list.get(1).count());

    // Month 5 should have 1 registration
    assertEquals(5, list.get(4).month());
    assertEquals(1, list.get(4).count());

    // Month 12 should have 0
    assertEquals(12, list.get(11).month());
    assertEquals(0, list.get(11).count());
  }

  @Test
  void testRejectsNonAdminRole() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-chart-sec",
            ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS,
            mapper.valueToTree(new YearRequest(2026)));

    ResponseEnvelope response = dispatcher.dispatch(request, userSession);

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
    assertEquals("Admin privileges required", response.message());
  }

  @Test
  void testRejectsUnauthenticated() {
    SessionContext anon = new SessionContext("conn-anon");
    RequestEnvelope request =
        new RequestEnvelope(
            "req-chart-anon",
            ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS,
            mapper.valueToTree(new YearRequest(2026)));

    ResponseEnvelope response = dispatcher.dispatch(request, anon);

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
  }
}
