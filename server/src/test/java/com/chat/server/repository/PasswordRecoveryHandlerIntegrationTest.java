package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.enums.ActionType;
import com.chat.server.config.ProtocolConfig;
import com.chat.server.handler.ActionHandler;
import com.chat.server.handler.impl.PasswordRecoveryHandler;
import com.chat.server.service.PasswordResetEmailSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({
  ProtocolConfig.class,
  PasswordRecoveryHandler.class,
  PasswordRecoveryHandlerIntegrationTest.MailTestConfig.class
})
class PasswordRecoveryHandlerIntegrationTest extends RepositoryTestSupport {
  @Autowired ActionHandler passwordRecoveryHandler;
  @Autowired ObjectMapper mapper;
  @Autowired RecordingEmailSender emailSender;
  @Autowired JdbcTemplate jdbc;

  @Test
  void savesOnlyTheMailedPasswordHashAndCanAuthenticateWithIt() throws Exception {
    long userId = newUser("uPasswordRecovery");
    String email = users.findById(userId).orElseThrow().email();
    String originalHash = users.findById(userId).orElseThrow().passwordHash();
    emailSender.reset();
    try {
      RequestEnvelope request = request(email);

      var response = passwordRecoveryHandler.handle(request, null);

      assertTrue(response.success());
      assertEquals(0, response.data().size());
      assertEquals(email, emailSender.recipient);
      var stored = users.findById(userId).orElseThrow();
      assertNotEquals(originalHash, stored.passwordHash());
      assertTrue(BCrypt.checkpw(emailSender.temporaryPassword, stored.passwordHash()));
    } finally {
      jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
    }
  }

  @Test
  void rollsBackThePasswordChangeWhenSmtpDeliveryFails() {
    long userId = newUser("uPasswordMailFail");
    String email = users.findById(userId).orElseThrow().email();
    String originalHash = users.findById(userId).orElseThrow().passwordHash();
    emailSender.reset();
    emailSender.fail = true;
    try {
      assertThrows(
          IllegalStateException.class, () -> passwordRecoveryHandler.handle(request(email), null));

      assertEquals(originalHash, users.findById(userId).orElseThrow().passwordHash());
      assertEquals(1, emailSender.sendCalls);
    } finally {
      jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
    }
  }

  @Test
  void handlerIsProxiedSoItsTransactionalBoundaryIsApplied() {
    assertTrue(AopUtils.isAopProxy(passwordRecoveryHandler));
    assertEquals(ActionType.REQUEST_PASSWORD_RESET, passwordRecoveryHandler.action());
  }

  private RequestEnvelope request(String email) {
    return new RequestEnvelope(
        "password-recovery-integration",
        ActionType.REQUEST_PASSWORD_RESET,
        mapper.valueToTree(new PasswordRecoveryRequest(email)));
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class MailTestConfig {
    @Bean
    RecordingEmailSender passwordResetEmailSender() {
      return new RecordingEmailSender();
    }
  }

  static class RecordingEmailSender implements PasswordResetEmailSender {
    private String recipient;
    private String temporaryPassword;
    private int sendCalls;
    private boolean fail;

    void reset() {
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
