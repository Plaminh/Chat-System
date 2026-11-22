package com.chat.common.dto.group;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.text;

public record RenameGroupRequest(long groupId, String name) {
  public RenameGroupRequest {
    groupId = positive(groupId, "groupId");
    name = text(name, "name");
  }
}
