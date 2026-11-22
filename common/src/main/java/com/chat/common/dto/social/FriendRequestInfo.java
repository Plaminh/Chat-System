package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.FriendRequestStatus;
import java.time.OffsetDateTime;

public record FriendRequestInfo(
    long friendRequestId,
    UserSummary sender,
    UserSummary receiver,
    FriendRequestStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime respondedAt) {
  public FriendRequestInfo {
    friendRequestId = positive(friendRequestId, "friendRequestId");
    sender = required(sender, "sender");
    receiver = required(receiver, "receiver");
    status = required(status, "status");
    createdAt = required(createdAt, "createdAt");
  }
}
