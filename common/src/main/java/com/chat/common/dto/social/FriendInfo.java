package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record FriendInfo(UserSummary user, OffsetDateTime friendshipCreatedAt) {
  public FriendInfo {
    user = required(user, "user");
    friendshipCreatedAt = required(friendshipCreatedAt, "friendshipCreatedAt");
  }
}
