package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;

public record LoginHistoryRequest(long userId, PageRequest page) {
  public LoginHistoryRequest {
    userId = positive(userId, "userId");
    page = required(page, "page");
  }
}
