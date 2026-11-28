package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.UserRole;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class GetProfileHandler implements ActionHandler {
  private final ObjectMapper mapper;
  private final UserRepository users;
  private final OnlineSessionRegistry sessions;

  public GetProfileHandler(
      ObjectMapper mapper, UserRepository users, OnlineSessionRegistry sessions) {
    this.mapper = mapper;
    this.users = users;
    this.sessions = sessions;
  }

  @Override
  public ActionType action() {
    return ActionType.GET_PROFILE;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    if (!session.hasRole(UserRole.USER)) {
      return error(request, ErrorCode.UNAUTHORIZED, "User privileges required");
    }

    JsonNode data = request.data();
    if (data == null || !data.isObject() || !data.isEmpty()) {
      throw new ProtocolValidationException(
          ErrorCode.INVALID_PAYLOAD, request.requestId(), "GET_PROFILE expects an empty object");
    }

    User user = users.findById(session.getUserId()).orElse(null);
    if (user == null) {
      return error(request, ErrorCode.NOT_FOUND, "Account was not found");
    }

    return success(request, UserAccountDetailSupport.from(user, sessions));
  }

  private ResponseEnvelope success(RequestEnvelope request, UserAccountDetail detail) {
    return new ResponseEnvelope(request.requestId(), true, mapper.valueToTree(detail), null, null);
  }

  private ResponseEnvelope error(RequestEnvelope request, ErrorCode code, String message) {
    return new ResponseEnvelope(request.requestId(), false, null, code, message);
  }
}
