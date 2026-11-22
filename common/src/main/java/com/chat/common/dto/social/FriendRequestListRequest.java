package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.optionalText;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.enums.FriendRequestDirection;
import com.chat.common.enums.FriendRequestStatus;

public record FriendRequestListRequest(
    String query, FriendRequestStatus status, FriendRequestDirection direction, PageRequest page) {
  public FriendRequestListRequest {
    query = optionalText(query, "query");
    page = required(page, "page");
  }
}
