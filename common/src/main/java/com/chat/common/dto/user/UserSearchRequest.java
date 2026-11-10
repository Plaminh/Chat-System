package com.chat.common.dto.user;

import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.dto.common.PageRequest;

public record UserSearchRequest(String query, PageRequest page) {
  public UserSearchRequest {
    query = text(query, "query");
    page = required(page, "page");
  }
}
