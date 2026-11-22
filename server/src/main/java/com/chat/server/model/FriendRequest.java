package com.chat.server.model;

import com.chat.common.enums.FriendRequestStatus;
import java.time.OffsetDateTime;

/** One row of {@code friend_requests}. */
public record FriendRequest(
    long friendRequestId,
    long senderUserId,
    long receiverUserId,
    FriendRequestStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime respondedAt) {}
