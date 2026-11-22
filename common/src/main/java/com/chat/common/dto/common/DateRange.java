package com.chat.common.dto.common;

import static com.chat.common.protocol.ContractChecks.required;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {
  public DateRange {
    from = required(from, "from");
    to = required(to, "to");
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("from must not be after to");
    }
  }
}
