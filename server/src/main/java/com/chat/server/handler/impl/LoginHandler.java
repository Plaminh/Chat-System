package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.AuthenticationResult;
import com.chat.common.dto.auth.LoginRequest;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.service.SessionLifecycle;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class LoginHandler implements ActionHandler {
  private final ObjectMapper mapper;
  private final UserRepository users;
  private final SessionLifecycle sessionLifecycle;

  public LoginHandler(
      ObjectMapper mapper, UserRepository users, SessionLifecycle sessionLifecycle) {
    this.mapper = mapper;
    this.users = users;
    this.sessionLifecycle = sessionLifecycle;
  }

  @Override
  public ActionType action() {
    return ActionType.LOGIN;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    LoginRequest payload = ProtocolValidator.decodePayload(mapper, request, LoginRequest.class);

    Optional<User> user = users.findByUsername(payload.username());
    if (user.isEmpty() && payload.username().contains("@")) {
      user = users.findByEmail(payload.username());
    }
    if (user.isEmpty()) {
      return error(request, ErrorCode.INVALID_CREDENTIALS, "Invalid username/email or password");
    }
    User account = user.orElseThrow();
    if (account.accountStatus() == AccountStatus.LOCKED) {
      return error(request, ErrorCode.ACCOUNT_LOCKED, "Account is locked");
    }
    if (account.accountStatus() == AccountStatus.DELETED) {
      return error(request, ErrorCode.ACCOUNT_DELETED, "Account is deleted");
    }
    if (!BCrypt.checkpw(payload.password(), account.passwordHash())) {
      return error(request, ErrorCode.INVALID_CREDENTIALS, "Invalid username/email or password");
    }

    session.authenticate(account.userId(), account.username(), account.role());
    sessionLifecycle.begin(session, account.userId());

    UserSummary summary =
        new UserSummary(
            account.userId(),
            account.username(),
            account.fullName(),
            AccountStatus.ACTIVE,
            PresenceStatus.ONLINE);
    AuthenticationResult result = new AuthenticationResult(summary, OffsetDateTime.now());
    return new ResponseEnvelope(request.requestId(), true, mapper.valueToTree(result), null, null);
  }

  private ResponseEnvelope error(RequestEnvelope request, ErrorCode code, String message) {
    return new ResponseEnvelope(request.requestId(), false, null, code, message);
  }
}
