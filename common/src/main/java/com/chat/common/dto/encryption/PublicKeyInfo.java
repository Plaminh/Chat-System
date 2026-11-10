package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.bytes;
import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import java.time.OffsetDateTime;

public record PublicKeyInfo(
    long publicKeyId,
    long userId,
    int keyVersion,
    String keyAlgorithm,
    String publicKeyFormat,
    byte[] publicKey,
    OffsetDateTime createdAt,
    OffsetDateTime retiredAt) {
  public PublicKeyInfo {
    publicKeyId = positive(publicKeyId, "publicKeyId");
    userId = positive(userId, "userId");
    keyVersion = positive(keyVersion, "keyVersion");
    publicKey = bytes(publicKey, "publicKey");
    createdAt = required(createdAt, "createdAt");
    if (!EncryptionAlgorithms.PUBLIC_KEY.equals(keyAlgorithm)
        || !EncryptionAlgorithms.PUBLIC_KEY_FORMAT.equals(publicKeyFormat)
        || publicKey.length != 32) {
      throw new IllegalArgumentException("public key metadata is invalid");
    }
  }

  @Override
  public byte[] publicKey() {
    return publicKey.clone();
  }
}
