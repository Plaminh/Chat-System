package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.bytes;
import static com.chat.common.protocol.ContractChecks.positive;

public record RecipientKeyEnvelope(
    long recipientPublicKeyId,
    String envelopeAlgorithm,
    byte[] encapsulatedKey,
    byte[] encryptedGroupKey) {
  public RecipientKeyEnvelope {
    recipientPublicKeyId = positive(recipientPublicKeyId, "recipientPublicKeyId");
    encapsulatedKey = bytes(encapsulatedKey, "encapsulatedKey");
    encryptedGroupKey = bytes(encryptedGroupKey, "encryptedGroupKey");
    if (!EncryptionAlgorithms.KEY_ENVELOPE.equals(envelopeAlgorithm)
        || encapsulatedKey.length != 32
        || encryptedGroupKey.length != 48) {
      throw new IllegalArgumentException("recipient key envelope metadata is invalid");
    }
  }

  @Override
  public byte[] encapsulatedKey() {
    return encapsulatedKey.clone();
  }

  @Override
  public byte[] encryptedGroupKey() {
    return encryptedGroupKey.clone();
  }
}
