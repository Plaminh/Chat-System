package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;

public record AdminUserFilter(
    String query, AccountStatus accountStatus, SortSpec sort, PageRequest page) {
  public AdminUserFilter {
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
