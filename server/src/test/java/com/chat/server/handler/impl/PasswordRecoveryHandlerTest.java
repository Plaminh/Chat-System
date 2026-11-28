package com.chat.server.handler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionDispatcher;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.service.PasswordResetEmailSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;

class PasswordRecoveryHandlerTest {
  private static final String EMAIL = "member@example.test";
  private final ObjectMapper mapper = new ProtocolConfig().objectMapper();
  private final FakeUserRepository users = new FakeUserRepository();
  private final RecordingEmailSender emailSender = new RecordingEmailSender();
  private PasswordRecoveryHandler handler;
  private ActionDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    users.reset();
    emailSender.reset();
    handler = new PasswordRecoveryHandler(mapper, users, emailSender);
    dispatcher = new ActionDispatcher(mapper, List.of(handler));
  }

  @Test
  void returnsTheSameEmptySuccessForAnUnknownEmailWithoutSendingMail() {
    ResponseEnvelope response =
        dispatcher.dispatch(request(ActionType.REQUEST_PASSWORD_RESET, EMAIL), null);

    assertTrue(response.success());
    assertTrue(response.data().isObject());
    assertEquals(0, response.data().size());
    assertEquals(1, users.findByEmailCalls);
    assertEquals(0, emailSender.sendCalls);
  }

  @Test
  void generatesHashesAndEmailsANewPasswordWithoutReturningIt() {
    users.addAccount();

    ResponseEnvelope response =
        dispatcher.dispatch(request(ActionType.REQUEST_PASSWORD_RESET, EMAIL), null);

    assertTrue(response.success());
    assertTrue(response.data().isObject());
    assertEquals(0, response.data().size());
    assertEquals(EMAIL, emailSender.recipient);
    assertTrue(emailSender.temporaryPassword.length() >= 16);
    assertNotEquals(users.originalHash, users.passwordHash);
    assertTrue(BCrypt.checkpw(emailSender.temporaryPassword, users.passwordHash));
    assertFalse(response.toString().contains(emailSender.temporaryPassword));
  }

  @Test
  void rejectsMalformedEmailBeforeLookingUpAnAccount() {
    ResponseEnvelope response =
        dispatcher.dispatch(request(ActionType.REQUEST_PASSWORD_RESET, "not-an-email"), null);

    assertFalse(response.success());
    assertEquals(ErrorCode.VALIDATION_FAILED, response.errorCode());
    assertEquals(0, users.findByEmailCalls);
  }

  @Test
  void mapsMailFailureToInternalErrorAndDoesNotUseDeprecatedResetAction() {
    users.addAccount();
    emailSender.fail = true;

    ResponseEnvelope mailFailure =
        dispatcher.dispatch(request(ActionType.REQUEST_PASSWORD_RESET, EMAIL), null);
    ResponseEnvelope deprecatedAction =
        dispatcher.dispatch(request(ActionType.RESET_PASSWORD, EMAIL), null);

    assertFalse(mailFailure.success());
    assertEquals(ErrorCode.INTERNAL_ERROR, mailFailure.errorCode());
    assertEquals(1, emailSender.sendCalls);
    assertFalse(deprecatedAction.success());
    assertEquals(ErrorCode.UNSUPPORTED_OPERATION, deprecatedAction.errorCode());
  }

  private RequestEnvelope request(ActionType action, String email) {
    return new RequestEnvelope(
        "password-recovery", action, mapper.valueToTree(new PasswordRecoveryRequest(email)));
  }

  private static final class FakeUserRepository extends UserRepository {
    private final String originalHash = BCrypt.hashpw("old-password", BCrypt.gensalt());
    private User account;
    private String passwordHash = originalHash;
    private int findByEmailCalls;

    private FakeUserRepository() {
      super(new JdbcTemplate());
    }

    private void reset() {
      account = null;
      passwordHash = originalHash;
      findByEmailCalls = 0;
    }

    private void addAccount() {
      OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-02T03:04:05Z");
      account =
          new User(
              42L,
              "member",
              passwordHash,
              EMAIL,
              "Member Name",
              "Existing address",
              LocalDate.of(2000, 1, 1),
              Gender.OTHER,
              UserRole.USER,
              com.chat.common.enums.AccountStatus.ACTIVE,
              null,
              null,
              null,
              timestamp,
              timestamp);
    }

    @Override
    public Optional<User> findByEmail(String email) {
      findByEmailCalls++;
      return account != null && account.email().equalsIgnoreCase(email)
          ? Optional.of(account)
          : Optional.empty();
    }

    @Override
    public boolean updatePasswordHash(long userId, String newPasswordHash) {
      if (account == null || account.userId() != userId) {
        return false;
      }
      passwordHash = newPasswordHash;
      return true;
    }
  }

  private static final class RecordingEmailSender implements PasswordResetEmailSender {
    private String recipient;
    private String temporaryPassword;
    private int sendCalls;
    private boolean fail;

    private void reset() {
      recipient = null;
      temporaryPassword = null;
      sendCalls = 0;
      fail = false;
    }

    @Override
    public void sendTemporaryPassword(String recipient, String temporaryPassword) {
      sendCalls++;
      this.recipient = recipient;
      this.temporaryPassword = temporaryPassword;
      if (fail) {
        throw new IllegalStateException("SMTP unavailable");
      }
    }
  }
}
