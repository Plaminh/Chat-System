package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.enums.FriendRequestStatus;

public record FriendRequestListRequest(FriendRequestStatus status, PageRequest page) {
  public FriendRequestListRequest {
    page = required(page, "page");
  }
}
