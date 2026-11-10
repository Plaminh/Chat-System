package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.PresenceStatus;
import java.time.OffsetDateTime;

public record PresenceUpdate(long userId, PresenceStatus presence, OffsetDateTime changedAt) {
  public PresenceUpdate {
    userId = positive(userId, "userId");
    presence = required(presence, "presence");
    changedAt = required(changedAt, "changedAt");
  }
}
