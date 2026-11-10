package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;

import com.chat.common.dto.encryption.EncryptedMessageContent;

public record SendMessageRequest(
    long conversationId, String body, EncryptedMessageContent encryptedContent) {
  public SendMessageRequest {
    conversationId = positive(conversationId, "conversationId");
    if ((body == null) == (encryptedContent == null)) {
      throw new IllegalArgumentException("exactly one content form is required");
    }
    if (body != null && body.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
  }
}
