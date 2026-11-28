package com.chat.server.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.model.User;
import com.chat.server.model.UserProfile;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

class ProfileActionHandlerTest {
  private static final long USER_ID = 42L;
  private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
  private final FakeUserRepository users = new FakeUserRepository();
  private final OnlineSessionRegistry sessions = new OnlineSessionRegistry(mapper);
  private GetProfileHandler getProfileHandler;
  private UpdateProfileHandler updateProfileHandler;

  @BeforeEach
  void setUp() {
    users.clear();
    getProfileHandler = new GetProfileHandler(mapper, users, sessions);
    updateProfileHandler = new UpdateProfileHandler(mapper, users, sessions);
  }

  @Test
  void getsTheAuthenticatedUsersAccountDetail() throws Exception {
    User user = user(USER_ID, "member", "member@example.test", "Existing Name");
    users.put(user);
    SessionContext session = authenticatedSession(USER_ID, "member");

    ResponseEnvelope response =
        getProfileHandler.handle(
            request(ActionType.GET_PROFILE, mapper.createObjectNode()), session);

    assertTrue(response.success());
    UserAccountDetail detail = mapper.treeToValue(response.data(), UserAccountDetail.class);
    assertEquals(USER_ID, detail.summary().userId());
    assertEquals("member@example.test", detail.email());
    assertEquals("Existing Name", detail.summary().fullName());
    assertEquals(PresenceStatus.OFFLINE, detail.summary().presence());
  }

  @Test
  void profileActionsRequireAnAuthenticatedSession() {
    ActionDispatcher dispatcher =
        new ActionDispatcher(mapper, List.of(getProfileHandler, updateProfileHandler));
    RequestEnvelope request =
        request(ActionType.UPDATE_PROFILE, profileRequest("member@example.test"));

    ResponseEnvelope response =
        dispatcher.dispatch(
            request(ActionType.GET_PROFILE, mapper.createObjectNode()),
            new SessionContext("anonymous"));
    ResponseEnvelope updateResponse =
        dispatcher.dispatch(request, new SessionContext("anonymous-update"));

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
    assertFalse(updateResponse.success());
    assertEquals(ErrorCode.UNAUTHENTICATED, updateResponse.errorCode());
    assertEquals(0, users.findCalls);
  }

  @Test
  void profileActionsRejectAnAdminSession() {
    ActionDispatcher dispatcher =
        new ActionDispatcher(mapper, List.of(getProfileHandler, updateProfileHandler));
    SessionContext adminSession = new SessionContext("admin-connection");
    adminSession.authenticate(USER_ID, "administrator", UserRole.ADMIN);

    ResponseEnvelope response =
        dispatcher.dispatch(
            request(ActionType.GET_PROFILE, mapper.createObjectNode()), adminSession);
    ResponseEnvelope updateResponse =
        dispatcher.dispatch(
            request(ActionType.UPDATE_PROFILE, profileRequest("member@example.test")),
            adminSession);

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHORIZED, response.errorCode());
    assertFalse(updateResponse.success());
    assertEquals(ErrorCode.UNAUTHORIZED, updateResponse.errorCode());
    assertEquals(0, users.findCalls);
  }

  @Test
  void returnsNotFoundWhenTheAuthenticatedAccountIsMissing() throws Exception {
    ResponseEnvelope response =
        getProfileHandler.handle(
            request(ActionType.GET_PROFILE, mapper.createObjectNode()),
            authenticatedSession(USER_ID, "member"));

    assertFalse(response.success());
    assertEquals(ErrorCode.NOT_FOUND, response.errorCode());
  }

  @Test
  void rejectsUnexpectedFieldsForGetProfile() {
    RequestEnvelope request =
        request(ActionType.GET_PROFILE, mapper.createObjectNode().put("userId", USER_ID));

    ResponseEnvelope response =
        new ActionDispatcher(mapper, List.of(getProfileHandler))
            .dispatch(request, authenticatedSession(USER_ID, "member"));

    assertFalse(response.success());
    assertEquals(ErrorCode.INVALID_PAYLOAD, response.errorCode());
    assertEquals(0, users.findCalls);
  }

  @Test
  void updateAllowsTheCurrentEmailAndReturnsThePersistedAccountDetail() throws Exception {
    User existing = user(USER_ID, "member", "member@example.test", "Existing Name");
    users.put(existing);

    UpdateProfileRequest payload =
        new UpdateProfileRequest(
            "Updated Name",
            "member@example.test",
            "New address",
            LocalDate.of(1999, 2, 3),
            Gender.FEMALE);
    ResponseEnvelope response =
        updateProfileHandler.handle(
            request(ActionType.UPDATE_PROFILE, payload), authenticatedSession(USER_ID, "member"));

    assertTrue(response.success());
    UserAccountDetail detail = mapper.treeToValue(response.data(), UserAccountDetail.class);
    assertEquals("Updated Name", detail.summary().fullName());
    assertEquals("New address", detail.address());
    assertEquals(LocalDate.of(1999, 2, 3), detail.dateOfBirth());
    assertEquals(Gender.FEMALE, detail.gender());
  }

  @Test
  void rejectsAnEmailUsedByAnotherAccountWithoutUpdating() throws Exception {
    User existing = user(USER_ID, "member", "member@example.test", "Existing Name");
    users.put(existing);
    users.put(user(43L, "other", "other@example.test", "Other Name"));
    UpdateProfileRequest payload =
        new UpdateProfileRequest(
            "Updated Name",
            "other@example.test",
            "New address",
            LocalDate.of(1999, 2, 3),
            Gender.OTHER);

    ResponseEnvelope response =
        updateProfileHandler.handle(
            request(ActionType.UPDATE_PROFILE, payload), authenticatedSession(USER_ID, "member"));

    assertFalse(response.success());
    assertEquals(ErrorCode.ALREADY_EXISTS, response.errorCode());
    assertEquals(0, users.updateCalls);
  }

  @Test
  void mapsConcurrentEmailConflictToAlreadyExists() {
    users.put(user(USER_ID, "member", "member@example.test", "Existing Name"));
    users.duplicateOnUpdate = true;
    ActionDispatcher dispatcher = new ActionDispatcher(mapper, List.of(updateProfileHandler));

    ResponseEnvelope response =
        dispatcher.dispatch(
            request(ActionType.UPDATE_PROFILE, profileRequest("new@example.test")),
            authenticatedSession(USER_ID, "member"));

    assertFalse(response.success());
    assertEquals(ErrorCode.ALREADY_EXISTS, response.errorCode());
  }

  @Test
  void rejectsInvalidEmailBeforeChangingTheProfile() {
    users.put(user(USER_ID, "member", "member@example.test", "Existing Name"));

    ResponseEnvelope response =
        new ActionDispatcher(mapper, List.of(updateProfileHandler))
            .dispatch(
                request(ActionType.UPDATE_PROFILE, profileRequest("not-an-email")),
                authenticatedSession(USER_ID, "member"));

    assertFalse(response.success());
    assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    assertEquals(0, users.updateCalls);
  }

  private RequestEnvelope request(ActionType action, Object payload) {
    return new RequestEnvelope("profile-request", action, mapper.valueToTree(payload));
  }

  private UpdateProfileRequest profileRequest(String email) {
    return new UpdateProfileRequest(
        "Updated Name", email, "New address", LocalDate.of(1999, 2, 3), Gender.FEMALE);
  }

  private SessionContext authenticatedSession(long userId, String username) {
    SessionContext session = new SessionContext("connection-" + userId);
    session.authenticate(userId, username, UserRole.USER);
    return session;
  }

  private User user(long userId, String username, String email, String fullName) {
    OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-02T03:04:05Z");
    return new User(
        userId,
        username,
        "redacted-hash",
        email,
        fullName,
        "Existing address",
        LocalDate.of(2000, 1, 1),
        Gender.OTHER,
        UserRole.USER,
        AccountStatus.ACTIVE,
        null,
        null,
        null,
        timestamp,
        timestamp);
  }

  private static final class FakeUserRepository extends UserRepository {
    private final Map<Long, User> accounts = new HashMap<>();
    private int findCalls;
    private int updateCalls;
    private boolean duplicateOnUpdate;

    private FakeUserRepository() {
      super(new JdbcTemplate());
    }

    private void clear() {
      accounts.clear();
      findCalls = 0;
      updateCalls = 0;
      duplicateOnUpdate = false;
    }

    private void put(User user) {
      accounts.put(user.userId(), user);
    }

    @Override
    public Optional<User> findById(long userId) {
      findCalls++;
      return Optional.ofNullable(accounts.get(userId));
    }

    @Override
    public boolean existsByEmail(String email, long exceptUserId) {
      return accounts.values().stream()
          .anyMatch(user -> user.userId() != exceptUserId && user.email().equalsIgnoreCase(email));
    }

    @Override
    public boolean updateProfile(long userId, UserProfile profile) {
      updateCalls++;
      if (duplicateOnUpdate) {
        throw new DuplicateKeyException("Unique email constraint violated");
      }
      User current = accounts.get(userId);
      if (current == null) {
        return false;
      }
      accounts.put(
          userId,
          new User(
              current.userId(),
              current.username(),
              current.passwordHash(),
              profile.email(),
              profile.fullName(),
              profile.address(),
              profile.dateOfBirth(),
              profile.gender(),
              current.role(),
              current.accountStatus(),
              current.lockedAt(),
              current.deletedAt(),
              current.passwordChangedAt(),
              current.createdAt(),
              OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)));
      return true;
    }
  }
}
