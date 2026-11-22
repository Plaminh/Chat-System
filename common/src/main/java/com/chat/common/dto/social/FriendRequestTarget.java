package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;

public record FriendRequestTarget(long targetUserId) {
  public FriendRequestTarget {
    targetUserId = positive(targetUserId, "targetUserId");
  }
}
