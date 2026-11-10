package com.chat.common.dto.encryption;

import static com.chat.common.protocol.ContractChecks.bytes;
import static com.chat.common.protocol.ContractChecks.positive;

public record EncryptedMessageContent(
    int keyVersion, byte[] ciphertext, byte[] nonce, byte[] authenticationTag) {
  public EncryptedMessageContent {
    keyVersion = positive(keyVersion, "keyVersion");
    ciphertext = bytes(ciphertext, "ciphertext");
    nonce = bytes(nonce, "nonce");
    authenticationTag = bytes(authenticationTag, "authenticationTag");
    if (ciphertext.length == 0) {
      throw new IllegalArgumentException("ciphertext must not be empty");
    }
    if (nonce.length != 12) {
      throw new IllegalArgumentException("nonce must contain 12 bytes");
    }
    if (authenticationTag.length != 16) {
      throw new IllegalArgumentException("authenticationTag must contain 16 bytes");
    }
  }

  @Override
  public byte[] ciphertext() {
    return ciphertext.clone();
  }

  @Override
  public byte[] nonce() {
    return nonce.clone();
  }

  @Override
  public byte[] authenticationTag() {
    return authenticationTag.clone();
  }
}
