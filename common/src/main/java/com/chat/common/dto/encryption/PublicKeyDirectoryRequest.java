package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.list;

import java.util.HashSet;
import java.util.List;

public record PublicKeyDirectoryRequest(List<Long> userIds) {
  public PublicKeyDirectoryRequest {
    userIds = list(userIds, "userIds");
    if (userIds.isEmpty()
        || userIds.stream().anyMatch(id -> id <= 0)
        || new HashSet<>(userIds).size() != userIds.size()) {
      throw new IllegalArgumentException("userIds must be unique positive IDs");
    }
  }
}
