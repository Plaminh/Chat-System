package com.chat.server.model;

import java.time.OffsetDateTime;

/** One row of {@code user_public_keys}. */
public record UserPublicKey(
    long userPublicKeyId,
    long userId,
    int keyVersion,
    String keyAlgorithm,
    String publicKeyFormat,
    byte[] publicKey,
    OffsetDateTime createdAt,
    OffsetDateTime retiredAt) {}
