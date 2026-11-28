package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.model.UserProfile;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UpdateProfileHandler implements ActionHandler {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  private final ObjectMapper mapper;
  private final UserRepository users;
  private final OnlineSessionRegistry sessions;

  public UpdateProfileHandler(
      ObjectMapper mapper, UserRepository users, OnlineSessionRegistry sessions) {
    this.mapper = mapper;
    this.users = users;
    this.sessions = sessions;
  }

  @Override
  public ActionType action() {
    return ActionType.UPDATE_PROFILE;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    if (!session.hasRole(UserRole.USER)) {
      return error(request, ErrorCode.UNAUTHORIZED, "User privileges required");
    }

    UpdateProfileRequest payload =
        ProtocolValidator.decodePayload(mapper, request, UpdateProfileRequest.class);
    String fullName = payload.fullName().trim();
    String email = payload.email().trim();
    String address = payload.address().trim();
    validate(request, fullName, email, address, payload.dateOfBirth());

    long userId = session.getUserId();
    User currentUser = users.findById(userId).orElse(null);
    if (currentUser == null) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }
    if (users.existsByEmail(email, userId)) {
      return error(request, ErrorCode.ALREADY_EXISTS, "Email already exists");
    }

    UserProfile profile =
        new UserProfile(fullName, email, address, payload.dateOfBirth(), payload.gender());
    if (!users.updateProfile(userId, profile)) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }

    User updatedUser = users.findById(userId).orElse(null);
    if (updatedUser == null) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }
    return new ResponseEnvelope(
        request.requestId(),
        true,
        mapper.valueToTree(UserAccountDetailSupport.from(updatedUser, sessions)),
        null,
        null);
  }

  private void validate(
      RequestEnvelope request, String fullName, String email, String address, LocalDate dateOfBirth)
      throws ProtocolValidationException {
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw validation(request, "Email address is invalid");
    }
    if (fullName.length() < 2 || fullName.length() > 100) {
      throw validation(request, "Full name must contain 2-100 characters");
    }
    if (address.isBlank()) {
      throw validation(request, "Address is required");
    }
    if (!dateOfBirth.isBefore(LocalDate.now())) {
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
