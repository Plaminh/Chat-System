package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.admin.AdminPasswordResetRequest;
import com.chat.common.dto.admin.AdminUserFilter;
import com.chat.common.dto.admin.LoginHistoryEntry;
import com.chat.common.dto.admin.LoginHistoryRequest;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.dto.auth.PasswordResetRequest;
import com.chat.common.dto.auth.RegisterRequest;
import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.dto.social.FriendListRequest;
import com.chat.common.dto.social.FriendRequestListRequest;
import com.chat.common.dto.statistics.FriendStatistic;
import com.chat.common.dto.statistics.FriendStatisticsRequest;
import com.chat.common.dto.statistics.UserActivityStatistic;
import com.chat.common.dto.statistics.UserActivityStatisticsRequest;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ComparisonOperator;
import com.chat.common.enums.FriendRequestDirection;
import com.chat.common.enums.FriendRequestStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.SortDirection;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinalContractCorrectionTest {
  private static final PageRequest PAGE = new PageRequest(0, 20);
  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.parse("2026-11-10T09:15:00+07:00");

  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void supportsGlobalAndPerUserLoginHistoryWithChronologicalSorting() throws Exception {
    LoginHistoryRequest global =
        new LoginHistoryRequest(null, new SortSpec("loggedInAt", SortDirection.ASC), PAGE);
    LoginHistoryRequest filtered =
        new LoginHistoryRequest(7L, new SortSpec("loggedInAt", SortDirection.DESC), PAGE);

    assertEquals(
        global, mapper.readValue(mapper.writeValueAsString(global), LoginHistoryRequest.class));
    assertEquals(7L, filtered.userId());
    assertNull(global.userId());

    LoginHistoryEntry entry =
        new LoginHistoryEntry(
            11, 7, "alice", "Alice Nguyen", CREATED_AT, CREATED_AT.plusMinutes(30));
    assertEquals(
        entry, mapper.readValue(mapper.writeValueAsString(entry), LoginHistoryEntry.class));
    assertEquals(List.of(), new PageResult<LoginHistoryEntry>(List.of(), 0, 20, 0, 0).items());

    assertThrows(
        IllegalArgumentException.class,
        () -> new LoginHistoryRequest(0L, new SortSpec("loggedInAt", SortDirection.ASC), PAGE));
    assertThrows(NullPointerException.class, () -> new LoginHistoryRequest(null, null, PAGE));
  }

  @Test
  void representsNewRegistrationListsThroughTheAdminUserFilter() throws Exception {
    DateRange createdAt =
        new DateRange(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"));
    AdminUserFilter filter =
        new AdminUserFilter(
            "alice",
            AccountStatus.ACTIVE,
            createdAt,
            new SortSpec("createdAt", SortDirection.DESC),
            PAGE);

    assertEquals(
        filter, mapper.readValue(mapper.writeValueAsString(filter), AdminUserFilter.class));
    assertEquals(createdAt, filter.createdAtRange());
  }

  @Test
  void roundTripsFriendAndFriendRequestNameFilters() throws Exception {
    FriendListRequest friends = new FriendListRequest("Alice", PAGE);
    FriendRequestListRequest incoming =
        new FriendRequestListRequest(
            "Alice", FriendRequestStatus.PENDING, FriendRequestDirection.INCOMING, PAGE);

    assertEquals(
        friends, mapper.readValue(mapper.writeValueAsString(friends), FriendListRequest.class));
    assertEquals(
        incoming,
        mapper.readValue(mapper.writeValueAsString(incoming), FriendRequestListRequest.class));
    assertEquals(PresenceStatus.ONLINE, user().presence());

    assertThrows(IllegalArgumentException.class, () -> new FriendListRequest(" ", PAGE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new FriendRequestListRequest(
                " ", FriendRequestStatus.PENDING, FriendRequestDirection.INCOMING, PAGE));
  }

  @Test
  void supportsRequiredFriendAndActivityStatisticsFilters() throws Exception {
    SortSpec createdAtSort = new SortSpec("createdAt", SortDirection.ASC);
    FriendStatisticsRequest friendRequest =
        new FriendStatisticsRequest(
            "Alice", new NumericFilter(ComparisonOperator.GT, 2), null, createdAtSort, PAGE);
    UserActivityStatisticsRequest activityRequest =
        new UserActivityStatisticsRequest(
            new DateRange(LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30")),
            "Alice",
            new NumericFilter(ComparisonOperator.EQ, 5),
            new NumericFilter(ComparisonOperator.LT, 8),
            null,
            createdAtSort,
            PAGE);

    assertEquals(
        friendRequest,
        mapper.readValue(mapper.writeValueAsString(friendRequest), FriendStatisticsRequest.class));
    assertEquals(
        activityRequest,
        mapper.readValue(
            mapper.writeValueAsString(activityRequest), UserActivityStatisticsRequest.class));

    FriendStatistic friendStatistic = new FriendStatistic(user(), CREATED_AT, 3, 7);
    UserActivityStatistic activityStatistic =
        new UserActivityStatistic(user(), CREATED_AT, activityRequest.range(), 5, 4, 2);
    assertEquals(CREATED_AT, friendStatistic.accountCreatedAt());
    assertEquals(CREATED_AT, activityStatistic.accountCreatedAt());
    assertTrue(mapper.valueToTree(activityRequest).has("chattedUsers"));
    assertFalse(mapper.valueToTree(activityRequest).has("chattedContacts"));
    assertTrue(mapper.valueToTree(activityStatistic).has("chattedUserCount"));
    assertFalse(mapper.valueToTree(activityStatistic).has("chattedContactCount"));
    assertThrows(
        IllegalArgumentException.class, () -> new NumericFilter(ComparisonOperator.GT, -1));
  }

  @Test
  @SuppressWarnings("deprecation")
  void recoveryRequestTriggersServerWorkWithoutCarryingOrReturningAPassword() throws Exception {
    PasswordRecoveryRequest recovery = new PasswordRecoveryRequest("alice@example.test");
    var json = mapper.valueToTree(recovery);
    ResponseEnvelope response =
        new ResponseEnvelope("req-recovery", true, mapper.createObjectNode(), null, null);

    assertEquals("alice@example.test", json.get("email").asText());
    assertFalse(json.has("password"));
    assertFalse(json.has("newPassword"));
    assertFalse(json.has("resetCode"));
    assertEquals(0, response.data().size());
    assertEquals(ActionType.RESET_PASSWORD, ActionType.valueOf("RESET_PASSWORD"));
  }

  @Test
  @SuppressWarnings("deprecation")
  void diagnosticTextRedactsCredentialsAndRawEnvelopePayloads() {
    String secret = "very-secret-value";
    List<Object> sensitive =
        List.of(
            new LoginRequest("alice", secret),
            new PasswordChangeRequest(secret, secret),
            new PasswordResetRequest(secret, secret),
            new AdminPasswordResetRequest(7, secret),
            new RegisterRequest(
                "alice",
                secret,
                "alice@example.test",
                "Alice Nguyen",
                "1 Example Street",
                LocalDate.parse("2000-05-20"),
                Gender.FEMALE));

    sensitive.forEach(value -> assertFalse(value.toString().contains(secret)));
    assertEquals(
        secret, mapper.valueToTree(new LoginRequest("alice", secret)).get("password").asText());

    RequestEnvelope envelope =
        new RequestEnvelope(
            "req-secret",
            ActionType.LOGIN,
            mapper.createObjectNode().put("username", "alice").put("password", secret));
    assertFalse(envelope.toString().contains(secret));
    assertFalse(envelope.toString().contains("password"));
    assertTrue(envelope.toString().contains("LOGIN"));
  }

  private UserSummary user() {
    return new UserSummary(7, "alice", "Alice Nguyen", AccountStatus.ACTIVE, PresenceStatus.ONLINE);
  }
}
