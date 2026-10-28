package com.chat.user.model;

import java.util.Objects;

public record FriendRequest(UserProfile requester, Status status) {
  public enum Status {
    PENDING,
    ACCEPTED,
    DECLINED
  }

  public FriendRequest {
    Objects.requireNonNull(requester, "requester");
    Objects.requireNonNull(status, "status");
  }

  public FriendRequest withStatus(Status nextStatus) {
    return new FriendRequest(requester, nextStatus);
  }
}
