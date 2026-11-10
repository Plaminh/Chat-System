package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.FriendRequestDecision;

public record FriendRequestDecisionRequest(long friendRequestId, FriendRequestDecision decision) {
  public FriendRequestDecisionRequest {
    friendRequestId = positive(friendRequestId, "friendRequestId");
    decision = required(decision, "decision");
  }
}
