package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.GroupRole;
import java.time.OffsetDateTime;

public record GroupMemberInfo(UserSummary user, GroupRole role, OffsetDateTime joinedAt) {
  public GroupMemberInfo {
    user = required(user, "user");
    role = required(role, "role");
    joinedAt = required(joinedAt, "joinedAt");
  }
}
