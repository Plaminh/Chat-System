package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.AuthenticationResult;
import com.chat.common.dto.auth.RegisterRequest;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.NewUser;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class RegisterHandler implements ActionHandler {
  private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]{3,30}");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  private final ObjectMapper mapper;
  private final UserRepository users;

  public RegisterHandler(ObjectMapper mapper, UserRepository users) {
    this.mapper = mapper;
    this.users = users;
  }

  @Override
  public ActionType action() {
    return ActionType.REGISTER;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    RegisterRequest payload =
        ProtocolValidator.decodePayload(mapper, request, RegisterRequest.class);
    validate(request, payload);

    if (users.existsByUsername(payload.username())) {
      return error(request, ErrorCode.ALREADY_EXISTS, "Username already exists");
    }
    if (users.existsByEmail(payload.email(), 0)) {
      return error(request, ErrorCode.ALREADY_EXISTS, "Email already exists");
    }

    String passwordHash = BCrypt.hashpw(payload.password(), BCrypt.gensalt());
    long userId =
        users.insert(
            new NewUser(
                payload.username(),
                passwordHash,
                payload.email(),
                payload.fullName(),
                payload.address(),
                payload.dateOfBirth(),
                payload.gender(),
                UserRole.USER));

    var user =
        new UserSummary(
            userId,
            payload.username(),
            payload.fullName(),
            AccountStatus.ACTIVE,
            PresenceStatus.OFFLINE);
    AuthenticationResult result =
        new AuthenticationResult(user, OffsetDateTime.now());
    return new ResponseEnvelope(request.requestId(), true, mapper.valueToTree(result), null, null);
  }

  private void validate(RequestEnvelope request, RegisterRequest payload)
      throws ProtocolValidationException {
    if (!USERNAME_PATTERN.matcher(payload.username()).matches()) {
      throw validation(request, "Username must contain 3-30 letters, digits, or underscores");
    }
    if (!EMAIL_PATTERN.matcher(payload.email()).matches()) {
      throw validation(request, "Email address is invalid");
    }
    if (payload.fullName().trim().length() < 2 || payload.fullName().length() > 100) {
      throw validation(request, "Full name must contain 2-100 characters");
    }
    if (payload.address().isBlank()) {
      throw validation(request, "Address is required");
    }
    if (payload.password().length() < 8) {
      throw validation(request, "Password must contain at least 8 characters");
    }
    if (!payload.dateOfBirth().isBefore(LocalDate.now())) {
      throw validation(request, "Date of birth must be in the past");
    }
  }

  private ProtocolValidationException validation(RequestEnvelope request, String message) {
    return new ProtocolValidationException(
        ErrorCode.VALIDATION_FAILED, request.requestId(), message);
  }

  private ResponseEnvelope error(RequestEnvelope request, ErrorCode code, String message) {
    return new ResponseEnvelope(request.requestId(), false, null, code, message);
  }
}
