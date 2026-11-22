package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;

public record FriendRequestIdTarget(long friendRequestId) {
  public FriendRequestIdTarget {
    friendRequestId = positive(friendRequestId, "friendRequestId");
  }
}
