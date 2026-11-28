package com.chat.server.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;

class ChangePasswordHandlerTest {
  private static final long USER_ID = 42L;
  private static final String OLD_PASSWORD = "current-password";
  private static final String NEW_PASSWORD = "new-password-123";

  private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
  private final FakeUserRepository users = new FakeUserRepository();
  private ChangePasswordHandler handler;
  private ActionDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    users.reset();
    handler = new ChangePasswordHandler(mapper, users);
    dispatcher = new ActionDispatcher(mapper, List.<ActionHandler>of(handler));
  }

  @Test
  void changesTheAuthenticatedUsersPasswordAndReturnsAnEmptyObject() {
    users.addUser(UserRole.USER);

    ResponseEnvelope response =
        dispatcher.dispatch(
            request(OLD_PASSWORD, NEW_PASSWORD), authenticatedSession(UserRole.USER));

    assertTrue(response.success());
    assertTrue(response.data().isObject());
    assertEquals(0, response.data().size());
    assertEquals(1, users.updateCalls);
    assertFalse(BCrypt.checkpw(OLD_PASSWORD, users.passwordHash));
    assertTrue(BCrypt.checkpw(NEW_PASSWORD, users.passwordHash));
  }

  @Test
  void returnsInvalidCredentialsAndKeepsThePasswordWhenCurrentPasswordIsWrong() {
    users.addUser(UserRole.USER);

    ResponseEnvelope response =
        dispatcher.dispatch(
            request("incorrect-current-password", NEW_PASSWORD),
            authenticatedSession(UserRole.USER));

    assertFalse(response.success());
    assertEquals(ErrorCode.INVALID_CREDENTIALS, response.errorCode());
    assertEquals(0, users.updateCalls);
    assertTrue(BCrypt.checkpw(OLD_PASSWORD, users.passwordHash));
  }

  @Test
  void allowsAnAdminToChangeTheirOwnPassword() {
    users.addUser(UserRole.ADMIN);

    ResponseEnvelope response =
        dispatcher.dispatch(
            request(OLD_PASSWORD, NEW_PASSWORD), authenticatedSession(UserRole.ADMIN));

    assertTrue(response.success());
    assertTrue(BCrypt.checkpw(NEW_PASSWORD, users.passwordHash));
  }

  @Test
  void rejectsShortNewPasswordsWithoutUpdatingTheHash() {
    users.addUser(UserRole.USER);

    ResponseEnvelope response =
        dispatcher.dispatch(request(OLD_PASSWORD, "short"), authenticatedSession(UserRole.USER));

    assertFalse(response.success());
    assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    assertEquals(0, users.updateCalls);
    assertTrue(BCrypt.checkpw(OLD_PASSWORD, users.passwordHash));
  }

  @Test
  void rejectsUnauthenticatedRequestsBeforeReadingTheAccount() {
    users.addUser(UserRole.USER);

    ResponseEnvelope response =
        dispatcher.dispatch(request(OLD_PASSWORD, NEW_PASSWORD), new SessionContext("anonymous"));

    assertFalse(response.success());
    assertEquals(ErrorCode.UNAUTHENTICATED, response.errorCode());
    assertEquals(0, users.findCalls);
  }

  private RequestEnvelope request(String currentPassword, String newPassword) {
    return new RequestEnvelope(
        "change-password",
        ActionType.CHANGE_PASSWORD,
        mapper.valueToTree(new PasswordChangeRequest(currentPassword, newPassword)));
  }

  private SessionContext authenticatedSession(UserRole role) {
    SessionContext session = new SessionContext("change-password-session");
    session.authenticate(USER_ID, "member", role);
    return session;
  }

  private static final class FakeUserRepository extends UserRepository {
    private User account;
    private String passwordHash;
    private int findCalls;
    private int updateCalls;

    private FakeUserRepository() {
      super(new JdbcTemplate());
    }

    private void reset() {
      account = null;
      passwordHash = null;
      findCalls = 0;
      updateCalls = 0;
    }

    private void addUser(UserRole role) {
      OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-02T03:04:05Z");
      passwordHash = BCrypt.hashpw(OLD_PASSWORD, BCrypt.gensalt());
      account =
          new User(
              USER_ID,
              "member",
              passwordHash,
              "member@example.test",
              "Member Name",
              "Existing address",
              LocalDate.of(2000, 1, 1),
              Gender.OTHER,
              role,
              AccountStatus.ACTIVE,
              null,
              null,
              null,
              timestamp,
              timestamp);
    }

    @Override
    public Optional<User> findById(long userId) {
      findCalls++;
      return account != null && account.userId() == userId
          ? Optional.of(account)
          : Optional.empty();
    }

    @Override
    public boolean updatePasswordHash(long userId, String newPasswordHash) {
      updateCalls++;
      if (account == null || account.userId() != userId) {
        return false;
      }
      passwordHash = newPasswordHash;
      return true;
    }
  }
}
