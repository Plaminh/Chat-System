package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;

public record MessageSearchRequest(
    String query, Long conversationId, DateRange dateRange, PageRequest page) {
  public MessageSearchRequest {
    query = text(query, "query");
    page = required(page, "page");
    if (conversationId != null && conversationId <= 0) {
      throw new IllegalArgumentException("conversationId must be positive");
    }
  }
}
