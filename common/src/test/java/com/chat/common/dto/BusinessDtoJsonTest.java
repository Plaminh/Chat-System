package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.chat.MessageInfo;
import com.chat.common.dto.chat.SendMessageRequest;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.group.CreateGroupRequest;
import com.chat.common.dto.social.FriendRequestTarget;
import com.chat.common.dto.statistics.MonthlyCount;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.dto.writing.WritingAssistRequest;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.WritingAssistMode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusinessDtoJsonTest {
  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void roundTripsAccountAndSocialPayloads() throws Exception {
    LoginRequest login = new LoginRequest("alice", "request-only-password");
    LoginRequest decoded = mapper.readValue(mapper.writeValueAsString(login), LoginRequest.class);
    assertEquals(login, decoded);

    FriendRequestTarget target = new FriendRequestTarget(7);
    assertEquals(target, mapper.readValue(mapper.writeValueAsString(target), target.getClass()));
  }

  @Test
  void roundTripsChatGroupStatisticsAndWritingPayloads() throws Exception {
    UserSummary sender =
        new UserSummary(1, "alice", "Alice Nguyen", AccountStatus.ACTIVE, PresenceStatus.ONLINE);
    MessageInfo message =
        new MessageInfo(
            10,
            20,
            sender,
            "Hello",
            null,
            OffsetDateTime.parse("2026-11-10T09:15:00+07:00"),
            null,
            null);
    MessageInfo decoded = mapper.readValue(mapper.writeValueAsString(message), MessageInfo.class);
    assertEquals(message, decoded);

    CreateGroupRequest group = new CreateGroupRequest("Project", List.of(2L, 3L), false);
    assertEquals(group, mapper.readValue(mapper.writeValueAsString(group), group.getClass()));
    assertEquals(
        new MonthlyCount(2026, 11, 4),
        mapper.readValue("{\"year\":2026,\"month\":11,\"count\":4}", MonthlyCount.class));
    assertEquals(
        WritingAssistMode.REVISE,
        new WritingAssistRequest(WritingAssistMode.REVISE, "draft", null).mode());
  }

  @Test
  void usesNonNullEmptyPageCollections() throws Exception {
    PageResult<String> page = new PageResult<>(List.of(), 0, 20, 0, 0);
    String json = mapper.writeValueAsString(page);
    assertEquals(0, mapper.readTree(json).get("items").size());
    assertFalse(mapper.readTree(json).get("items").isNull());
  }

  @Test
  void rejectsInvalidLocalContractValues() {
    assertThrows(IllegalArgumentException.class, () -> new PageRequest(-1, 20));
    assertThrows(IllegalArgumentException.class, () -> new FriendRequestTarget(0));
    assertThrows(IllegalArgumentException.class, () -> new SendMessageRequest(1, null, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new WritingAssistRequest(WritingAssistMode.REVISE, " ", null));
  }
}
