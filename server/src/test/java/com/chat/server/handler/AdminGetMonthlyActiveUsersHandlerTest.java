package com.chat.server.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.common.YearRequest;
import com.chat.common.dto.statistics.MonthlyCount;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.server.handler.impl.AdminGetMonthlyActiveUsersHandler;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminGetMonthlyActiveUsersHandlerTest {
  private final ObjectMapper mapper = AuthHandlerTestSupport.MAPPER;
  private AuthHandlerTestSupport.StubUserActivityLogRepository activityLogRepository;
  private AdminGetMonthlyActiveUsersHandler handler;
  private ActionDispatcher dispatcher;
  private SessionContext adminSession;
  private SessionContext userSession;

  @BeforeEach
  void setUp() {
    activityLogRepository = AuthHandlerTestSupport.stubUserActivityLogRepository();
    handler = new AdminGetMonthlyActiveUsersHandler(mapper, activityLogRepository);
    dispatcher = new ActionDispatcher(mapper, List.of(handler));

    adminSession = new SessionContext("conn-admin");
    adminSession.authenticate(1L, "admin", UserRole.ADMIN);

    userSession = new SessionContext("conn-user");
    userSession.authenticate(2L, "normalUser", UserRole.USER);

    // In Jan 2026: user 10 opens 3 times, user 20 opens 1 time -> distinct active = 2
    activityLogRepository.logAppOpen(10L, OffsetDateTime.parse("2026-01-05T08:00:00+07:00"));
    activityLogRepository.logAppOpen(10L, OffsetDateTime.parse("2026-01-10T12:00:00+07:00"));
    activityLogRepository.logAppOpen(10L, OffsetDateTime.parse("2026-01-15T18:00:00+07:00"));
    activityLogRepository.logAppOpen(20L, OffsetDateTime.parse("2026-01-20T09:00:00+07:00"));

    // In Feb 2026: user 10 opens 1 time -> distinct active = 1
    activityLogRepository.logAppOpen(10L, OffsetDateTime.parse("2026-02-14T10:00:00+07:00"));

    // In Dec 2026: user 30 opens 1 time -> distinct active = 1
    activityLogRepository.logAppOpen(30L, OffsetDateTime.parse("2026-12-25T20:00:00+07:00"));
  }

  @Test
  void testReturnsTwelveMonthsWithDistinctActiveCounts() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-act-1",
            ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS,
            mapper.valueToTree(new YearRequest(2026)));

    ResponseEnvelope response = dispatcher.dispatch(request, adminSession);

    assertTrue(response.success());
    assertNull(response.errorCode());

    List<MonthlyCount> list =
        mapper.convertValue(response.data(), new TypeReference<List<MonthlyCount>>() {});

    assertNotNull(list);
    assertEquals(12, list.size());

    // Month 1 should have 2 distinct users (even though user 10 logged 3 times)
    assertEquals(1, list.get(0).month());
    assertEquals(2026, list.get(0).year());
    assertEquals(2, list.get(0).count());

    // Month 2 should have 1 distinct user
    assertEquals(2, list.get(1).month());
    assertEquals(1, list.get(1).count());

    // Month 3 to 11 should have 0
    for (int m = 3; m <= 11; m++) {
      assertEquals(0, list.get(m - 1).count(), "Month " + m + " should be 0");
    }

    // Month 12 should have 1 distinct user
    assertEquals(12, list.get(11).month());
    assertEquals(1, list.get(11).count());
  }

  @Test
  void testRejectsNonAdminRole() {
    RequestEnvelope request =
        new RequestEnvelope(
            "req-act-sec",
            ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS,
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
            "req-act-anon",
            ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS,
            mapper.valueToTree(new YearRequest(2026)));

    ResponseEnvelope response = dispatcher.dispatch(request, anon);

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
  }
}
