package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.server.model.ConversationKeyEnvelope;
import com.chat.server.model.ConversationKeyVersion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code conversation_key_versions} and {@code conversation_key_envelopes}. The
 * server stores only client-encrypted group keys and never sees a plaintext key.
 */
@Repository
public class ConversationKeyRepository {
  private static final String VERSION_COLUMNS =
      "v.conversation_key_version_id, v.conversation_id, v.key_version, v.encryption_algorithm,"
          + " v.created_at, v.retired_at";

  private static final RowMapper<ConversationKeyVersion> VERSION =
      (rs, n) ->
          new ConversationKeyVersion(
              rs.getLong("conversation_key_version_id"),
              rs.getLong("conversation_id"),
              rs.getInt("key_version"),
              rs.getString("encryption_algorithm"),
              timestamp(rs, "created_at"),
              timestamp(rs, "retired_at"));

  private static final RowMapper<ConversationKeyEnvelope> ENVELOPE =
      (rs, n) ->
          new ConversationKeyEnvelope(
              rs.getLong("conversation_key_envelope_id"),
              rs.getLong("conversation_key_version_id"),
              rs.getLong("recipient_public_key_id"),
              rs.getString("envelope_algorithm"),
              rs.getBytes("encapsulated_key"),
              rs.getBytes("encrypted_group_key"),
              timestamp(rs, "created_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public ConversationKeyRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Inserts a key epoch and returns its row ID. Retire the previous epoch in the same transaction.
   */
  public long insertVersion(long conversationId, int keyVersion) {
    return jdbc.queryForObject(
        "INSERT INTO conversation_key_versions (conversation_id, key_version)"
            + " VALUES (:conversation, :version) RETURNING conversation_key_version_id",
        Map.of("conversation", conversationId, "version", keyVersion),
        Long.class);
  }

  /** Retires all active epochs of the conversation; returns the number retired. */
  public int retireActiveVersions(long conversationId) {
    return jdbc.update(
        "UPDATE conversation_key_versions SET retired_at = CURRENT_TIMESTAMP"
            + " WHERE conversation_id = :conversation AND retired_at IS NULL",
        Map.of("conversation", conversationId));
  }

  /** The newest active epoch, if any. */
  public Optional<ConversationKeyVersion> findActiveVersion(long conversationId) {
    return jdbc
        .query(
            "SELECT "
                + VERSION_COLUMNS
                + " FROM conversation_key_versions v WHERE v.conversation_id = :conversation"
                + " AND v.retired_at IS NULL ORDER BY v.key_version DESC LIMIT 1",
            Map.of("conversation", conversationId),
            VERSION)
        .stream()
        .findFirst();
  }

  public Optional<ConversationKeyVersion> findVersion(long conversationId, int keyVersion) {
    return jdbc
        .query(
            "SELECT "
                + VERSION_COLUMNS
                + " FROM conversation_key_versions v"
                + " WHERE v.conversation_id = :conversation AND v.key_version = :version",
            Map.of("conversation", conversationId, "version", keyVersion),
            VERSION)
        .stream()
        .findFirst();
  }

  /** Highest epoch number of the conversation, or 0 when none. */
  public int findLatestVersionNumber(long conversationId) {
    return jdbc.queryForObject(
        "SELECT coalesce(max(key_version), 0) FROM conversation_key_versions"
            + " WHERE conversation_id = :conversation",
        Map.of("conversation", conversationId),
        Integer.class);
  }

  /** Stores one recipient envelope and returns its ID. */
  public long insertEnvelope(
      long conversationKeyVersionId,
      long recipientPublicKeyId,
      byte[] encapsulatedKey,
      byte[] encryptedGroupKey) {
    return jdbc.queryForObject(
        "INSERT INTO conversation_key_envelopes (conversation_key_version_id,"
            + " recipient_public_key_id, encapsulated_key, encrypted_group_key)"
            + " VALUES (:version, :recipient, :encapsulated, :encrypted)"
            + " RETURNING conversation_key_envelope_id",
        Map.of(
            "version", conversationKeyVersionId,
            "recipient", recipientPublicKeyId,
            "encapsulated", encapsulatedKey,
            "encrypted", encryptedGroupKey),
        Long.class);
  }

  /** All envelopes of one epoch. */
  public List<ConversationKeyEnvelope> findEnvelopes(long conversationKeyVersionId) {
    return jdbc.query(
        "SELECT e.* FROM conversation_key_envelopes e"
            + " WHERE e.conversation_key_version_id = :version ORDER BY e.recipient_public_key_id",
        Map.of("version", conversationKeyVersionId),
        ENVELOPE);
  }

  /** The envelope of one epoch addressed to any public key owned by {@code userId}. */
  public Optional<ConversationKeyEnvelope> findEnvelopeForUser(
      long conversationId, int keyVersion, long userId) {
    return jdbc
        .query(
            "SELECT e.* FROM conversation_key_envelopes e"
                + " JOIN conversation_key_versions v"
                + " ON v.conversation_key_version_id = e.conversation_key_version_id"
                + " JOIN user_public_keys k ON k.user_public_key_id = e.recipient_public_key_id"
                + " WHERE v.conversation_id = :conversation AND v.key_version = :version"
                + " AND k.user_id = :user ORDER BY k.key_version DESC LIMIT 1",
            Map.of("conversation", conversationId, "version", keyVersion, "user", userId),
            ENVELOPE)
        .stream()
        .findFirst();
  }
}
