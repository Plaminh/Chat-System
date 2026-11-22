package com.chat.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.social.FriendInfo;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultJsonTest {
  private static final OffsetDateTime CREATED_AT =
      OffsetDateTime.parse("2026-11-18T10:15:00+07:00");

  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void deserializesTypedUserSummaryItems() {
    UserSummary user = user();
    ResponseEnvelope response = success(new PageResult<>(List.of(user), 0, 20, 1, 1));

    PageResult<UserSummary> decoded =
        mapper.convertValue(response.data(), new TypeReference<PageResult<UserSummary>>() {});

    assertEquals(List.of(user), decoded.items());
    assertInstanceOf(UserSummary.class, decoded.items().getFirst());
    assertEquals(0, decoded.page());
    assertEquals(20, decoded.size());
    assertEquals(1, decoded.totalItems());
    assertEquals(1, decoded.totalPages());
  }

  @Test
  void deserializesTypedFriendInfoItemsWithOffsetTime() {
    FriendInfo friend = new FriendInfo(user(), CREATED_AT);
    ResponseEnvelope response = success(new PageResult<>(List.of(friend), 0, 10, 1, 1));

    PageResult<FriendInfo> decoded =
        mapper.convertValue(response.data(), new TypeReference<PageResult<FriendInfo>>() {});

    assertInstanceOf(FriendInfo.class, decoded.items().getFirst());
    assertEquals(CREATED_AT, decoded.items().getFirst().friendshipCreatedAt());
    assertEquals(user(), decoded.items().getFirst().user());
  }

  @Test
  void deserializesTypedAdminUserInfoWithNestedDtoFields() {
    UserAccountDetail account =
        new UserAccountDetail(
            user(),
            "alice@example.test",
            "1 Example Street",
            LocalDate.parse("2000-05-20"),
            Gender.FEMALE,
            UserRole.USER,
            CREATED_AT,
            CREATED_AT.plusHours(1));
    AdminUserInfo adminUser = new AdminUserInfo(account);
    ResponseEnvelope response = success(new PageResult<>(List.of(adminUser), 1, 5, 6, 2));

    PageResult<AdminUserInfo> decoded =
        mapper.convertValue(response.data(), new TypeReference<PageResult<AdminUserInfo>>() {});

    assertInstanceOf(AdminUserInfo.class, decoded.items().getFirst());
    assertEquals(account, decoded.items().getFirst().account());
    assertEquals(CREATED_AT, decoded.items().getFirst().account().createdAt());
  }

  @Test
  void preservesEmptyTypedCollectionsAndMetadata() {
    ResponseEnvelope response = success(new PageResult<UserSummary>(List.of(), 0, 20, 0, 0));

    PageResult<UserSummary> decoded =
        mapper.convertValue(response.data(), new TypeReference<PageResult<UserSummary>>() {});

    assertEquals(List.of(), decoded.items());
    assertEquals(0, decoded.totalItems());
    assertEquals(0, decoded.totalPages());
  }

  @Test
  void rejectsInvalidPaginationMetadata() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), -1, 20, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 0, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 101, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 20, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 20, 0, -1));

    var invalidWirePage =
        mapper.readTree("{\"items\":[],\"page\":-1,\"size\":20,\"totalItems\":0,\"totalPages\":0}");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            mapper.convertValue(invalidWirePage, new TypeReference<PageResult<UserSummary>>() {}));
  }

  private ResponseEnvelope success(PageResult<?> page) {
    return new ResponseEnvelope("req-page", true, mapper.valueToTree(page), null, null);
  }

  private UserSummary user() {
    return new UserSummary(7, "alice", "Alice Nguyen", AccountStatus.ACTIVE, PresenceStatus.ONLINE);
  }
}
