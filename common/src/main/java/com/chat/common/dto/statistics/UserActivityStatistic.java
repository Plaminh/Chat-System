package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.user.UserSummary;

public record UserActivityStatistic(
    UserSummary user,
    DateRange range,
    long appOpenCount,
    long chattedContactCount,
    long chattedGroupCount) {
  public UserActivityStatistic {
    user = required(user, "user");
    range = required(range, "range");
    if (appOpenCount < 0 || chattedContactCount < 0 || chattedGroupCount < 0) {
      throw new IllegalArgumentException("activity counts must not be negative");
    }
  }
}
