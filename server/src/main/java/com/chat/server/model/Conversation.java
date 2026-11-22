package com.chat.server.model;

import com.chat.common.enums.ConversationType;
import java.time.OffsetDateTime;

/** One row of {@code conversations}; {@code name} and {@code ownerUserId} are null for direct. */
public record Conversation(
    long conversationId,
    ConversationType type,
    String name,
    Long ownerUserId,
    boolean encryptionEnabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
