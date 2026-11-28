package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.service.PasswordResetEmailSender;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PasswordRecoveryHandler implements ActionHandler {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final String PASSWORD_CHARACTERS =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private static final int PASSWORD_LENGTH = 24;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final ObjectMapper mapper;
  private final UserRepository users;
  private final PasswordResetEmailSender emailSender;

  public PasswordRecoveryHandler(
      ObjectMapper mapper, UserRepository users, PasswordResetEmailSender emailSender) {
    this.mapper = mapper;
    this.users = users;
    this.emailSender = emailSender;
  }

  @Override
  public ActionType action() {
    return ActionType.REQUEST_PASSWORD_RESET;
  }

  @Override
  @Transactional
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    PasswordRecoveryRequest payload =
        ProtocolValidator.decodePayload(mapper, request, PasswordRecoveryRequest.class);
    if (!EMAIL_PATTERN.matcher(payload.email()).matches()) {
      throw new ProtocolValidationException(
          ErrorCode.VALIDATION_FAILED, request.requestId(), "Email address is invalid");
    }

    Optional<User> account = users.findByEmail(payload.email());
    if (account.isPresent()) {
      User user = account.orElseThrow();
      String temporaryPassword = generateTemporaryPassword();
      String passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt());
      if (!users.updatePasswordHash(user.userId(), passwordHash)) {
        throw new IllegalStateException("Could not update the account password");
      }
      emailSender.sendTemporaryPassword(user.email(), temporaryPassword);
    }

    return new ResponseEnvelope(request.requestId(), true, mapper.createObjectNode(), null, null);
  }

  private String generateTemporaryPassword() {
    StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
    for (int index = 0; index < PASSWORD_LENGTH; index++) {
      password.append(
          PASSWORD_CHARACTERS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARACTERS.length())));
    }
    return password.toString();
  }
}
