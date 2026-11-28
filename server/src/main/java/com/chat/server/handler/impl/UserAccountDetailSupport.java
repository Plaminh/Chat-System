package com.chat.server.handler.impl;

import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.PresenceStatus;
import com.chat.server.model.User;
import com.chat.server.session.OnlineSessionRegistry;

final class UserAccountDetailSupport {
  private UserAccountDetailSupport() {}

  static UserAccountDetail from(User user, OnlineSessionRegistry sessions) {
    UserSummary summary =
        new UserSummary(
            user.userId(),
            user.username(),
            user.fullName(),
            user.accountStatus(),
            sessions.isUserOnline(user.userId()) ? PresenceStatus.ONLINE : PresenceStatus.OFFLINE);
    return new UserAccountDetail(
        summary,
        user.email(),
        user.address(),
        user.dateOfBirth(),
        user.gender(),
        user.role(),
        user.createdAt(),
        user.updatedAt());
  }
}
