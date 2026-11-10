package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;

public record UserActivityStatisticsRequest(
    DateRange range,
    NumericFilter appOpens,
    NumericFilter chattedContacts,
    NumericFilter chattedGroups,
    SortSpec sort,
    PageRequest page) {
  public UserActivityStatisticsRequest {
    range = required(range, "range");
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
