package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;

public record MessageHistoryRequest(long conversationId, PageRequest page) {
  public MessageHistoryRequest {
    conversationId = positive(conversationId, "conversationId");
    page = required(page, "page");
  }
}
