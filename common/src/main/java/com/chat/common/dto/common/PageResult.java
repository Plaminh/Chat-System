package com.chat.common.dto.common;

import static com.chat.common.protocol.ContractChecks.list;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
  public PageResult {
    items = list(items, "items");
    if (page < 0 || size < 1 || totalItems < 0 || totalPages < 0) {
      throw new IllegalArgumentException("page metadata is invalid");
    }
  }
}
