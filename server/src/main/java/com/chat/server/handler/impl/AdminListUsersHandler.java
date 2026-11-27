package com.chat.server.handler.impl;

import com.chat.common.dto.RequestEnvelope;
import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.admin.AdminUserFilter;
import com.chat.common.dto.admin.AdminUserInfo;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.PresenceStatus;
import com.chat.common.exception.ProtocolValidationException;
import com.chat.common.protocol.ProtocolValidator;
import com.chat.server.exception.InvalidSortFieldException;
import com.chat.server.handler.ActionHandler;
import com.chat.server.model.User;
import com.chat.server.repository.UserRepository;
import com.chat.server.session.OnlineSessionRegistry;
import com.chat.server.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AdminListUsersHandler implements ActionHandler {
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt");

  private final ObjectMapper mapper;
  private final UserRepository userRepository;
  private final OnlineSessionRegistry sessionRegistry;

  public AdminListUsersHandler(
      ObjectMapper mapper,
      UserRepository userRepository,
      OnlineSessionRegistry sessionRegistry) {
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.sessionRegistry = sessionRegistry;
  }

  @Override
  public ActionType action() {
    return ActionType.ADMIN_LIST_USERS;
  }

  @Override
  public ResponseEnvelope handle(RequestEnvelope request, SessionContext session)
      throws ProtocolValidationException {
    String requestId = request.requestId();
    AdminUserFilter filter =
        ProtocolValidator.decodePayload(mapper, request, AdminUserFilter.class);

    if (filter.sort() != null && !ALLOWED_SORT_FIELDS.contains(filter.sort().field())) {
      throw new InvalidSortFieldException(filter.sort().field(), List.of("name", "createdAt"));
    }

    if (filter.createdAtRange() != null) {
      if (filter.createdAtRange().from() != null
          && filter.createdAtRange().to() != null
          && filter.createdAtRange().from().isAfter(filter.createdAtRange().to())) {
        return new ResponseEnvelope(
            requestId,
            false,
            null,
            ErrorCode.VALIDATION_FAILED,
            "Invalid date range: start date must be before or equal to end date");
      }
    }

    PageResult<User> pageResult =
        userRepository.findForAdmin(
            filter.query(),
            filter.accountStatus(),
            filter.createdAtRange(),
            filter.sort(),
            filter.page());

    List<AdminUserInfo> items =
        pageResult.items().stream().map(this::toAdminUserInfo).toList();

    PageResult<AdminUserInfo> result =
        new PageResult<>(
            items,
            pageResult.page(),
            pageResult.size(),
            pageResult.totalItems(),
            pageResult.totalPages());

    return new ResponseEnvelope(requestId, true, mapper.valueToTree(result), null, null);
  }

  private AdminUserInfo toAdminUserInfo(User user) {
    PresenceStatus presence =
        (sessionRegistry != null && sessionRegistry.isUserOnline(user.userId()))
            ? PresenceStatus.ONLINE
            : PresenceStatus.OFFLINE;

    UserSummary summary =
        new UserSummary(
            user.userId(),
            user.username(),
            user.fullName(),
            user.accountStatus(),
            presence);

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

    return new AdminUserInfo(detail);
  }
}
