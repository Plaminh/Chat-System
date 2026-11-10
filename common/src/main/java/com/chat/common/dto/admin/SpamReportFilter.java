package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.SpamReportStatus;

public record SpamReportFilter(
    DateRange dateRange,
    String reportedUsername,
    SpamReportStatus status,
    SortSpec sort,
    PageRequest page) {
  public SpamReportFilter {
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
