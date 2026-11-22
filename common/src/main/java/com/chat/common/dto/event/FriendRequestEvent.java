package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.social.FriendRequestInfo;

public record FriendRequestEvent(FriendRequestInfo friendRequest) {
  public FriendRequestEvent {
    friendRequest = required(friendRequest, "friendRequest");
  }
}
