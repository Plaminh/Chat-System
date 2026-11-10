package com.chat.common.dto.statistics;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.SortSpec;

public record FriendStatisticsRequest(
    NumericFilter directFriends,
    NumericFilter friendsOfFriends,
    SortSpec sort,
    PageRequest page) {
  public FriendStatisticsRequest {
    sort = required(sort, "sort");
    page = required(page, "page");
  }
}
