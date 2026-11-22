package com.chat.server.model;

import java.time.OffsetDateTime;

/** One group-key epoch from {@code conversation_key_versions}. */
public record ConversationKeyVersion(
    long conversationKeyVersionId,
    long conversationId,
    int keyVersion,
    String encryptionAlgorithm,
    OffsetDateTime createdAt,
    OffsetDateTime retiredAt) {}
