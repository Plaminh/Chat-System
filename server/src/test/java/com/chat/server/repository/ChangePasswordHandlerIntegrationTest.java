package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.impl.ChangePasswordHandler;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

class ChangePasswordHandlerIntegrationTest extends RepositoryTestSupport {
  private final ObjectMapper mapper = new ProtocolConfig().objectMapper();

  @Test
  void persistsTheNewPasswordAndInvalidatesTheOldPassword() {
    inRollback(
        () -> {
          long userId = newUser("uChangePassword");
          String oldPassword = "old-current-password";
          String newPassword = "new-password-123";
          users.updatePasswordHash(userId, BCrypt.hashpw(oldPassword, BCrypt.gensalt()));
          String oldHash = users.findById(userId).orElseThrow().passwordHash();
          ChangePasswordHandler handler = new ChangePasswordHandler(mapper, users);

          ResponseEnvelope response =
              handle(
                  handler,
                  request(oldPassword, newPassword),
                  authenticatedSession(userId, UserRole.USER));

          assertTrue(response.success());
          assertEquals(0, response.data().size());
          String storedHash = users.findById(userId).orElseThrow().passwordHash();
          assertFalse(oldHash.equals(storedHash));
          assertFalse(BCrypt.checkpw(oldPassword, storedHash));
          assertTrue(BCrypt.checkpw(newPassword, storedHash));
        });
  }

  @Test
  void wrongCurrentPasswordReturnsInvalidCredentialsAndLeavesPostgresUnchanged() {
    inRollback(
        () -> {
          long userId = newUser("uChangePasswordWrong");
          String originalHash = users.findById(userId).orElseThrow().passwordHash();
          ChangePasswordHandler handler = new ChangePasswordHandler(mapper, users);

          ResponseEnvelope response =
              handle(
                  handler,
                  request("wrong-current-password", "new-password-123"),
                  authenticatedSession(userId, UserRole.USER));

          assertFalse(response.success());
          assertEquals(ErrorCode.INVALID_CREDENTIALS, response.errorCode());
          assertEquals(originalHash, users.findById(userId).orElseThrow().passwordHash());
        });
  }

  private RequestEnvelope request(String currentPassword, String newPassword) {
    return new RequestEnvelope(
        "change-password-integration",
        ActionType.CHANGE_PASSWORD,
        mapper.valueToTree(new PasswordChangeRequest(currentPassword, newPassword)));
  }

  private SessionContext authenticatedSession(long userId, UserRole role) {
    SessionContext session = new SessionContext("change-password-" + userId);
    session.authenticate(userId, "integration-user", role);
    return session;
  }

  private ResponseEnvelope handle(
      ChangePasswordHandler handler, RequestEnvelope request, SessionContext session) {
    try {
      return handler.handle(request, session);
    } catch (Exception exception) {
      throw new AssertionError("Change password handler unexpectedly failed", exception);
    }
  }
}
