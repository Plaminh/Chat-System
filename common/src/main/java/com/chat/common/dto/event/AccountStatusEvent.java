package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.AccountStatus;
import java.time.OffsetDateTime;

public record AccountStatusEvent(long userId, AccountStatus status, OffsetDateTime changedAt) {
  public AccountStatusEvent {
    userId = positive(userId, "userId");
    status = required(status, "status");
    changedAt = required(changedAt, "changedAt");
  }
}
