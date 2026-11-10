package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record FriendStatistic(
    UserSummary user,
    OffsetDateTime accountCreatedAt,
    int directFriendCount,
    int friendsOfFriendsCount) {
  public FriendStatistic {
    user = required(user, "user");
    accountCreatedAt = required(accountCreatedAt, "accountCreatedAt");
    if (directFriendCount < 0 || friendsOfFriendsCount < 0) {
      throw new IllegalArgumentException("friend counts must not be negative");
    }
  }
}
