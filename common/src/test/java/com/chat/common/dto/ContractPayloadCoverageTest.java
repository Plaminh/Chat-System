package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.admin.AdminCreateUserRequest;
import com.chat.common.dto.admin.AdminUpdateUserRequest;
import com.chat.common.dto.auth.RegisterRequest;
import com.chat.common.dto.chat.MessageInfo;
import com.chat.common.dto.chat.SendMessageRequest;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.group.GroupInfo;
import com.chat.common.dto.group.GroupMemberInfo;
import com.chat.common.dto.group.GroupMembersRequest;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.GroupRole;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContractPayloadCoverageTest {
  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.parse("2026-11-10T09:15:00+07:00");

  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void acceptsPaginationBoundariesAndRejectsOutsideValues() {
    assertEquals(new PageRequest(0, 1), new PageRequest(0, 1));
    assertEquals(new PageRequest(4, 100), new PageRequest(4, 100));

    assertThrows(IllegalArgumentException.class, () -> new PageRequest(-1, 20));
    assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 0));
    assertThrows(IllegalArgumentException.class, () -> new PageRequest(0, 101));
  }

  @Test
  void preservesEmptyCollectionsAndRejectsNullCollections() throws Exception {
    PageResult<String> page = new PageResult<>(List.of(), 0, 20, 0, 0);
    PageResult<?> decoded = mapper.readValue(mapper.writeValueAsString(page), PageResult.class);

    assertEquals(List.of(), decoded.items());
    assertThrows(NullPointerException.class, () -> new PageResult<>(null, 0, 20, 0, 0));
    assertThrows(NullPointerException.class, () -> new GroupMembersRequest(1, null));
  }

  @Test
  void roundTripsRepresentativeUserAdminAndGroupContracts() throws Exception {
    RegisterRequest registration =
        new RegisterRequest(
            "alice",
            "request-only-password",
            "alice@example.test",
            "Alice Nguyen",
            "1 Example Street",
            LocalDate.parse("2000-05-20"),
            Gender.FEMALE);
    AdminCreateUserRequest create = new AdminCreateUserRequest(registration, UserRole.USER);
    assertEquals(
        create, mapper.readValue(mapper.writeValueAsString(create), AdminCreateUserRequest.class));

    UpdateProfileRequest profile =
        new UpdateProfileRequest(
            "Alice Nguyen",
            "alice@example.test",
            "1 Example Street",
            LocalDate.parse("2000-05-20"),
            Gender.FEMALE);
    AdminUpdateUserRequest update = new AdminUpdateUserRequest(7, profile, UserRole.USER);
    assertEquals(
        update, mapper.readValue(mapper.writeValueAsString(update), AdminUpdateUserRequest.class));

    UserSummary user = user(7, "alice", "Alice Nguyen");
    UserAccountDetail detail =
        new UserAccountDetail(
            user,
            "alice@example.test",
            "1 Example Street",
            LocalDate.parse("2000-05-20"),
            Gender.FEMALE,
            UserRole.USER,
            CREATED_AT,
            CREATED_AT.plusHours(1));
    assertEquals(
        detail, mapper.readValue(mapper.writeValueAsString(detail), UserAccountDetail.class));

    GroupMemberInfo owner = new GroupMemberInfo(user, GroupRole.ADMIN, CREATED_AT);
    GroupMemberInfo member =
        new GroupMemberInfo(user(8, "bob", "Bob Tran"), GroupRole.MEMBER, CREATED_AT);
    GroupInfo group =
        new GroupInfo(
            20, "Project", 7, true, List.of(owner, member), CREATED_AT, CREATED_AT.plusMinutes(5));
    assertEquals(group, mapper.readValue(mapper.writeValueAsString(group), GroupInfo.class));
  }

  @Test
  void serializesCalendarDatesAndOffsetDateTimesAsIsoText() throws Exception {
    UserAccountDetail detail =
        new UserAccountDetail(
            user(7, "alice", "Alice Nguyen"),
            "alice@example.test",
            "1 Example Street",
            LocalDate.parse("2000-05-20"),
            Gender.FEMALE,
            UserRole.USER,
            CREATED_AT,
            CREATED_AT.plusHours(1));

    var json = mapper.valueToTree(detail);
    assertEquals("2000-05-20", json.get("dateOfBirth").asText());
    assertEquals("2026-11-10T09:15:00+07:00", json.get("createdAt").asText());
  }

  @Test
  void preservesRecalledStateWithoutOriginalContent() throws Exception {
    MessageInfo recalled =
        new MessageInfo(
            10,
            20,
            user(7, "alice", "Alice Nguyen"),
            null,
            null,
            CREATED_AT,
            CREATED_AT.plusMinutes(1),
            7L);
    MessageInfo decoded = mapper.readValue(mapper.writeValueAsString(recalled), MessageInfo.class);

    assertEquals(recalled, decoded);
    assertNull(decoded.body());
    assertNull(decoded.encryptedContent());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MessageInfo(
                10,
                20,
                user(7, "alice", "Alice Nguyen"),
                "must not survive recall",
                null,
                CREATED_AT,
                CREATED_AT.plusMinutes(1),
                7L));
    assertThrows(IllegalArgumentException.class, () -> new SendMessageRequest(20, null, null));
  }

  private UserSummary user(long userId, String username, String fullName) {
    return new UserSummary(userId, username, fullName, AccountStatus.ACTIVE, PresenceStatus.ONLINE);
  }
}
