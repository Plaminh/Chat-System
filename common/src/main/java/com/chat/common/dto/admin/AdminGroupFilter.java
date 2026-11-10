package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;

public record AdminGroupFilter(String query, SortSpec sort, PageRequest page) {
  public AdminGroupFilter {
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
