package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordHandler implements ActionHandler {
  private static final int MINIMUM_PASSWORD_LENGTH = 8;

  private final ObjectMapper mapper;
  private final UserRepository users;

  public ChangePasswordHandler(ObjectMapper mapper, UserRepository users) {
    this.mapper = mapper;
    this.users = users;
  }

  @Override
  public ActionType action() {
    return ActionType.CHANGE_PASSWORD;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    if (session == null || !session.isAuthenticated()) {
      return error(request, ErrorCode.UNAUTHENTICATED, "Authentication required");
    }
    if (!session.hasRole(UserRole.USER) && !session.hasRole(UserRole.ADMIN)) {
      return error(request, ErrorCode.UNAUTHORIZED, "User or Admin privileges required");
    }

    PasswordChangeRequest payload =
        ProtocolValidator.decodePayload(mapper, request, PasswordChangeRequest.class);
    if (payload.newPassword().length() < MINIMUM_PASSWORD_LENGTH) {
      throw new ProtocolValidationException(
          ErrorCode.VALIDATION_FAILED,
          request.requestId(),
          "New password must contain at least 8 characters");
    }

    User account = users.findById(session.getUserId()).orElse(null);
    if (account == null) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }
    if (account.accountStatus() == AccountStatus.LOCKED) {
      return error(request, ErrorCode.ACCOUNT_LOCKED, "Account is locked");
    }
    if (account.accountStatus() == AccountStatus.DELETED) {
      return error(request, ErrorCode.ACCOUNT_DELETED, "Account is deleted");
    }
    if (!BCrypt.checkpw(payload.currentPassword(), account.passwordHash())) {
      return error(request, ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect.");
    }

    String newPasswordHash = BCrypt.hashpw(payload.newPassword(), BCrypt.gensalt());
    if (!users.updatePasswordHash(account.userId(), newPasswordHash)) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }
    return new ResponseEnvelope(request.requestId(), true, mapper.createObjectNode(), null, null);
  }

  private ResponseEnvelope error(RequestEnvelope request, ErrorCode code, String message) {
    return new ResponseEnvelope(request.requestId(), false, null, code, message);
  }
}
