package com.chat.common.dto.common;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.ComparisonOperator;

public record NumericFilter(ComparisonOperator operator, long value) {
  public NumericFilter {
    operator = required(operator, "operator");
    if (value < 0) {
      throw new IllegalArgumentException("value must not be negative");
    }
  }
}
