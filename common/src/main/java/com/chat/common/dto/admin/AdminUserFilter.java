package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.optionalText;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;

public record AdminUserFilter(
    String query,
    AccountStatus accountStatus,
    DateRange createdAtRange,
    SortSpec sort,
    PageRequest page) {
  public AdminUserFilter {
    query = optionalText(query, "query");
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
