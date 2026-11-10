package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;

public record LoginHistoryRequest(Long userId, SortSpec sort, PageRequest page) {
  public LoginHistoryRequest {
    if (userId != null && userId <= 0) {
      throw new IllegalArgumentException("userId must be positive when provided");
    }
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
