package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import java.time.OffsetDateTime;

public record GroupKeyEpochInfo(
    long epochId,
    long groupId,
    int keyVersion,
    String encryptionAlgorithm,
    OffsetDateTime createdAt,
    OffsetDateTime retiredAt) {
  public GroupKeyEpochInfo {
    epochId = positive(epochId, "epochId");
    groupId = positive(groupId, "groupId");
    keyVersion = positive(keyVersion, "keyVersion");
    createdAt = required(createdAt, "createdAt");
    if (!EncryptionAlgorithms.MESSAGE.equals(encryptionAlgorithm)) {
      throw new IllegalArgumentException("unsupported encryptionAlgorithm");
    }
  }
}
