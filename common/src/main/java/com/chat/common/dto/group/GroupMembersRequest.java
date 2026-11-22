package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.list;
import static com.chat.common.protocol.ContractChecks.positive;

import java.util.HashSet;
import java.util.List;

public record GroupMembersRequest(long groupId, List<Long> userIds) {
  public GroupMembersRequest {
    groupId = positive(groupId, "groupId");
    userIds = list(userIds, "userIds");
    if (userIds.isEmpty()
        || userIds.stream().anyMatch(id -> id <= 0)
        || new HashSet<>(userIds).size() != userIds.size()) {
      throw new IllegalArgumentException("userIds must be unique positive IDs");
    }
  }
}
