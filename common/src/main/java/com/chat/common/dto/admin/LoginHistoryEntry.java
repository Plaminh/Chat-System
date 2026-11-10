package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import java.time.OffsetDateTime;

public record LoginHistoryEntry(
    long loginHistoryId,
    long userId,
    String username,
    String fullName,
    OffsetDateTime loggedInAt,
    OffsetDateTime loggedOutAt) {
  public LoginHistoryEntry {
    loginHistoryId = positive(loginHistoryId, "loginHistoryId");
    userId = positive(userId, "userId");
    username = text(username, "username");
    fullName = text(fullName, "fullName");
    loggedInAt = required(loggedInAt, "loggedInAt");
    if (loggedOutAt != null && loggedOutAt.isBefore(loggedInAt)) {
      throw new IllegalArgumentException("loggedOutAt must not precede loggedInAt");
    }
  }
}
