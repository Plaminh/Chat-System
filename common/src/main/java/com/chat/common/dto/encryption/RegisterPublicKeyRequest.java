package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.bytes;
import static com.chat.common.protocol.ContractChecks.positive;

public record RegisterPublicKeyRequest(
    int keyVersion, String keyAlgorithm, String publicKeyFormat, byte[] publicKey) {
  public RegisterPublicKeyRequest {
    keyVersion = positive(keyVersion, "keyVersion");
    publicKey = bytes(publicKey, "publicKey");
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
