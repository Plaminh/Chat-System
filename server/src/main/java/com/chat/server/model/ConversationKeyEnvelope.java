package com.chat.server.model;

import java.time.OffsetDateTime;

/** One recipient envelope from {@code conversation_key_envelopes}. */
public record ConversationKeyEnvelope(
    long conversationKeyEnvelopeId,
    long conversationKeyVersionId,
    long recipientPublicKeyId,
    String envelopeAlgorithm,
    byte[] encapsulatedKey,
    byte[] encryptedGroupKey,
    OffsetDateTime createdAt) {}
