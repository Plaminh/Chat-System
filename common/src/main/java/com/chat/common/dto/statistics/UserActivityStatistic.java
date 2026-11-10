package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record UserActivityStatistic(
    UserSummary user,
    OffsetDateTime accountCreatedAt,
    DateRange range,
    long appOpenCount,
    long chattedUserCount,
    long chattedGroupCount) {
  public UserActivityStatistic {
    user = required(user, "user");
    accountCreatedAt = required(accountCreatedAt, "accountCreatedAt");
    range = required(range, "range");
    if (appOpenCount < 0 || chattedUserCount < 0 || chattedGroupCount < 0) {
      throw new IllegalArgumentException("activity counts must not be negative");
    }
  }
}
