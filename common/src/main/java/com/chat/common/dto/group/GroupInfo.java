package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.list;
import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import java.time.OffsetDateTime;
import java.util.List;

public record GroupInfo(
    long groupId,
    String name,
    long ownerUserId,
    boolean encryptionEnabled,
    List<GroupMemberInfo> members,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public GroupInfo {
    groupId = positive(groupId, "groupId");
    name = text(name, "name");
    ownerUserId = positive(ownerUserId, "ownerUserId");
    members = list(members, "members");
    createdAt = required(createdAt, "createdAt");
    updatedAt = required(updatedAt, "updatedAt");
  }
}
