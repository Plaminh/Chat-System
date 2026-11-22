package com.chat.server.model;

import com.chat.common.enums.FriendRequestStatus;
import java.time.OffsetDateTime;

/** A friend request joined with both users. */
public record FriendRequestView(
    long friendRequestId,
    UserBrief sender,
    UserBrief receiver,
    FriendRequestStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime respondedAt) {}
