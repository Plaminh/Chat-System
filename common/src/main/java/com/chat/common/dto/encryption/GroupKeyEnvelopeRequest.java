package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.positive;

public record GroupKeyEnvelopeRequest(long groupId, int keyVersion) {
  public GroupKeyEnvelopeRequest {
    groupId = positive(groupId, "groupId");
    keyVersion = positive(keyVersion, "keyVersion");
  }
}
