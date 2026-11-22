package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record MessageSearchHit(
    long messageId,
    long conversationId,
    String conversationName,
    UserSummary sender,
    OffsetDateTime createdAt,
    String matchedText) {
  public MessageSearchHit {
    messageId = positive(messageId, "messageId");
    conversationId = positive(conversationId, "conversationId");
    createdAt = required(createdAt, "createdAt");
    matchedText = text(matchedText, "matchedText");
  }
}
