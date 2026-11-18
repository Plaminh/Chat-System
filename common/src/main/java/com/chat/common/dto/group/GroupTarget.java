package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.positive;

public record GroupTarget(long groupId) {
  public GroupTarget {
    groupId = positive(groupId, "groupId");
  }
}
