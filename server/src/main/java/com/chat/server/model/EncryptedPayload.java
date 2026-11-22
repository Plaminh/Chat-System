package com.chat.server.model;

/** AES-GCM ciphertext produced by a User Client; the server never decrypts it. */
public record EncryptedPayload(
    int keyVersion, byte[] ciphertext, byte[] nonce, byte[] authenticationTag) {}
