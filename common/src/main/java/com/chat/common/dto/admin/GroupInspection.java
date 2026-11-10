package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.group.GroupInfo;

public record GroupInspection(GroupInfo group, int memberCount) {
  public GroupInspection {
    group = required(group, "group");
    if (memberCount < 0 || memberCount != group.members().size()) {
      throw new IllegalArgumentException("memberCount is inconsistent");
    }
  }
}
