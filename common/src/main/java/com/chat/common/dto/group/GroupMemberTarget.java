package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.positive;

public record GroupMemberTarget(long groupId, long userId) {
  public GroupMemberTarget {
    groupId = positive(groupId, "groupId");
    userId = positive(userId, "userId");
  }
}
