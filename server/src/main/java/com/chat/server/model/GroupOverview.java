package com.chat.server.model;

import java.time.OffsetDateTime;

/** A group conversation with its active member count, for the Admin group list. */
public record GroupOverview(
    long conversationId,
    String name,
    long ownerUserId,
    boolean encryptionEnabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    long memberCount) {}
