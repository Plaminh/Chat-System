package com.chat.common.dto.chat;

import static com.chat.common.protocol.ContractChecks.positive;

public record MessageTarget(long messageId) {
  public MessageTarget {
    messageId = positive(messageId, "messageId");
  }
}
