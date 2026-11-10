package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.ConversationType;
import java.time.OffsetDateTime;

public record ConversationSummary(
    long conversationId,
    ConversationType type,
    String name,
    boolean encryptionEnabled,
    MessageInfo latestMessage,
    OffsetDateTime updatedAt) {
  public ConversationSummary {
    conversationId = positive(conversationId, "conversationId");
    type = required(type, "type");
    updatedAt = required(updatedAt, "updatedAt");
    if (type == ConversationType.DIRECT && (name != null || encryptionEnabled)) {
      throw new IllegalArgumentException("direct conversations cannot have a name or encryption");
    }
    if (type == ConversationType.GROUP && (name == null || name.isBlank())) {
      throw new IllegalArgumentException("group name must not be blank");
    }
  }
}
