package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import java.time.OffsetDateTime;

public record GroupKeyRotationEvent(
    long groupId, int requiredKeyVersion, OffsetDateTime occurredAt) {
  public GroupKeyRotationEvent {
    groupId = positive(groupId, "groupId");
    requiredKeyVersion = positive(requiredKeyVersion, "requiredKeyVersion");
    occurredAt = required(occurredAt, "occurredAt");
  }
}
