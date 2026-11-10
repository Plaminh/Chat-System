package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.list;
import static com.chat.common.protocol.ContractChecks.positive;

import java.util.HashSet;
import java.util.List;

public record GroupKeyRotationRequest(
    long groupId,
    int keyVersion,
    String encryptionAlgorithm,
    List<RecipientKeyEnvelope> recipientEnvelopes) {
  public GroupKeyRotationRequest {
    groupId = positive(groupId, "groupId");
    keyVersion = positive(keyVersion, "keyVersion");
    recipientEnvelopes = list(recipientEnvelopes, "recipientEnvelopes");
    if (!EncryptionAlgorithms.MESSAGE.equals(encryptionAlgorithm)
        || recipientEnvelopes.isEmpty()
        || new HashSet<>(
                    recipientEnvelopes.stream()
                        .map(RecipientKeyEnvelope::recipientPublicKeyId)
                        .toList())
                .size()
            != recipientEnvelopes.size()) {
      throw new IllegalArgumentException("group key rotation metadata is invalid");
    }
  }
}
