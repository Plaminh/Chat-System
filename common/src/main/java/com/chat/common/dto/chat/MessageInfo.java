package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.encryption.EncryptedMessageContent;
import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record MessageInfo(
    long messageId,
    long conversationId,
    UserSummary sender,
    String body,
    EncryptedMessageContent encryptedContent,
    OffsetDateTime createdAt,
    OffsetDateTime recalledAt,
    Long recalledByUserId) {
  public MessageInfo {
    messageId = positive(messageId, "messageId");
    conversationId = positive(conversationId, "conversationId");
    createdAt = required(createdAt, "createdAt");
    boolean recalled = recalledAt != null || recalledByUserId != null;
    if (recalled) {
      if (recalledAt == null || recalledByUserId == null || recalledByUserId <= 0) {
        throw new IllegalArgumentException("recall metadata must be complete");
      }
      if (body != null || encryptedContent != null) {
        throw new IllegalArgumentException("recalled messages must not expose content");
      }
    } else if ((body == null) == (encryptedContent == null)) {
      throw new IllegalArgumentException("message must contain exactly one content form");
    } else if (body != null && body.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
  }
}
