package com.chat.common.dto.event;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.GroupRole;
import java.time.OffsetDateTime;

public record GroupRoleEvent(
    long groupId, UserSummary user, GroupRole role, OffsetDateTime occurredAt) {
  public GroupRoleEvent {
    groupId = positive(groupId, "groupId");
    user = required(user, "user");
    role = required(role, "role");
    occurredAt = required(occurredAt, "occurredAt");
  }
}
