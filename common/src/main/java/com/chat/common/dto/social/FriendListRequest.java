package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.optionalText;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;

public record FriendListRequest(String query, PageRequest page) {
  public FriendListRequest {
    query = optionalText(query, "query");
    page = required(page, "page");
  }
}
