package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;

public record RegistrationCount(DateRange range, long count) {
  public RegistrationCount {
    range = required(range, "range");
    if (count < 0) {
      throw new IllegalArgumentException("count must not be negative");
    }
  }
}
