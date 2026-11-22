package com.chat.common.dto.social;

import static com.chat.common.protocol.ContractChecks.positive;

public record UserTarget(long targetUserId) {
  public UserTarget {
    targetUserId = positive(targetUserId, "targetUserId");
  }
}
