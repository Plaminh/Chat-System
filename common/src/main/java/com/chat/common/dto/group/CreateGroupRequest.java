package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.list;
import static com.chat.common.protocol.ContractChecks.text;

import java.util.HashSet;
import java.util.List;

public record CreateGroupRequest(
    String name, List<Long> initialMemberUserIds, boolean encryptionEnabled) {
  public CreateGroupRequest {
    name = text(name, "name");
    initialMemberUserIds = list(initialMemberUserIds, "initialMemberUserIds");
    if (initialMemberUserIds.isEmpty()
        || initialMemberUserIds.stream().anyMatch(id -> id <= 0)
        || new HashSet<>(initialMemberUserIds).size() != initialMemberUserIds.size()) {
      throw new IllegalArgumentException("initialMemberUserIds must be unique positive IDs");
    }
  }
}
