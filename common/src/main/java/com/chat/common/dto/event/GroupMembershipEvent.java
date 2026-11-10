package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.GroupMembershipChangeType;
import java.time.OffsetDateTime;

public record GroupMembershipEvent(
    long groupId,
    UserSummary user,
    GroupMembershipChangeType change,
    OffsetDateTime occurredAt) {
  public GroupMembershipEvent {
    groupId = positive(groupId, "groupId");
    user = required(user, "user");
    change = required(change, "change");
    occurredAt = required(occurredAt, "occurredAt");
  }
}
