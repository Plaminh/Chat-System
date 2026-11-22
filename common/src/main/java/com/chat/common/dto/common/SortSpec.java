package com.chat.common.dto.common;

import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.enums.SortDirection;

public record SortSpec(String field, SortDirection direction) {
  public SortSpec {
    field = text(field, "field");
    direction = required(direction, "direction");
  }
}
