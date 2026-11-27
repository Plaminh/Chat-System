package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.social.UserTarget;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AdminGetUserHandler implements ActionHandler {
  private final ObjectMapper mapper;
  private final UserRepository userRepository;
  private final OnlineSessionRegistry sessionRegistry;

  public AdminGetUserHandler(
      ObjectMapper mapper, UserRepository userRepository, OnlineSessionRegistry sessionRegistry) {
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.sessionRegistry = sessionRegistry;
  }

  @Override
  public ActionType action() {
    return ActionType.ADMIN_GET_USER;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session) throws Exception {
    String requestId = request.requestId();
    UserTarget target = ProtocolValidator.decodePayload(mapper, request, UserTarget.class);
    if (target == null) {
      return new ResponseEnvelope(
          requestId, false, null, ErrorCode.INVALID_REQUEST, "targetUserId is required");
    }

    Optional<User> userOpt = userRepository.findById(target.targetUserId());
    if (userOpt.isEmpty()) {
      return new ResponseEnvelope(requestId, false, null, ErrorCode.NOT_FOUND, "User not found");
    }

    User user = userOpt.get();
    PresenceStatus presence =
        (sessionRegistry != null && sessionRegistry.isUserOnline(user.userId()))
            ? PresenceStatus.ONLINE
            : PresenceStatus.OFFLINE;

    UserSummary summary =
        new UserSummary(
            user.userId(), user.username(), user.fullName(), user.accountStatus(), presence);

    UserAccountDetail detail =
        new UserAccountDetail(
            summary,
            user.email(),
            user.address(),
            user.dateOfBirth(),
            user.gender(),
            user.role(),
            user.createdAt(),
            user.updatedAt());

    AdminUserInfo adminUserInfo = new AdminUserInfo(detail);
    return new ResponseEnvelope(requestId, true, mapper.valueToTree(adminUserInfo), null, null);
  }
}
