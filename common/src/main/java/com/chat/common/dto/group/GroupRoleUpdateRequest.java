package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.GroupRole;

public record GroupRoleUpdateRequest(long groupId, long userId, GroupRole role) {
  public GroupRoleUpdateRequest {
    groupId = positive(groupId, "groupId");
    userId = positive(userId, "userId");
    role = required(role, "role");
  }
}
