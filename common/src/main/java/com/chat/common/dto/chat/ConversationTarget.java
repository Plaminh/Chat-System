package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;

public record ConversationTarget(long conversationId) {
  public ConversationTarget {
    conversationId = positive(conversationId, "conversationId");
  }
}
