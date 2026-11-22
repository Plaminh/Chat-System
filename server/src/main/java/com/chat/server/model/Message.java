package com.chat.server.model;

import java.time.OffsetDateTime;

/**
 * One row of {@code messages}. Plaintext rows use {@code body}; encrypted rows use the ciphertext
 * fields. An unsent message keeps only the placeholder body.
 */
public record Message(
    long messageId,
    long conversationId,
    Long senderUserId,
    String body,
    boolean contentEncrypted,
    byte[] ciphertext,
    Integer encryptionKeyVersion,
    byte[] encryptionNonce,
    byte[] authenticationTag,
    OffsetDateTime createdAt,
    OffsetDateTime unsentAt,
    Long unsentBy) {

  /** Body stored by the schema after a message is unsent. */
  public static final String UNSENT_BODY = "[Message unsent]";
}
