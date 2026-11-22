package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.server.model.UserPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code user_public_keys}. Each user has at most one active (unretired) key. */
@Repository
public class UserPublicKeyRepository {
  private static final String COLUMNS =
      "k.user_public_key_id, k.user_id, k.key_version, k.key_algorithm, k.public_key_format,"
          + " k.public_key, k.created_at, k.retired_at";

  private static final RowMapper<UserPublicKey> KEY =
      (rs, n) ->
          new UserPublicKey(
              rs.getLong("user_public_key_id"),
              rs.getLong("user_id"),
              rs.getInt("key_version"),
              rs.getString("key_algorithm"),
              rs.getString("public_key_format"),
              rs.getBytes("public_key"),
              timestamp(rs, "created_at"),
              timestamp(rs, "retired_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public UserPublicKeyRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Inserts a key with the default algorithm and format and returns its ID. Retire the previous
   * active key first in the same transaction.
   */
  public long insert(long userId, int keyVersion, byte[] publicKey) {
    return jdbc.queryForObject(
        "INSERT INTO user_public_keys (user_id, key_version, public_key)"
            + " VALUES (:userId, :version, :key) RETURNING user_public_key_id",
        Map.of("userId", userId, "version", keyVersion, "key", publicKey),
        Long.class);
  }

  /** Retires the user's active key; returns the number of retired rows (0 or 1). */
  public int retireActive(long userId) {
    return jdbc.update(
        "UPDATE user_public_keys SET retired_at = CURRENT_TIMESTAMP"
            + " WHERE user_id = :userId AND retired_at IS NULL",
        Map.of("userId", userId));
  }

  public Optional<UserPublicKey> findById(long userPublicKeyId) {
    return jdbc
        .query(
            "SELECT " + COLUMNS + " FROM user_public_keys k WHERE k.user_public_key_id = :id",
            Map.of("id", userPublicKeyId),
            KEY)
        .stream()
        .findFirst();
  }

  public Optional<UserPublicKey> findActive(long userId) {
    return jdbc
        .query(
            "SELECT "
                + COLUMNS
                + " FROM user_public_keys k WHERE k.user_id = :userId AND k.retired_at IS NULL",
            Map.of("userId", userId),
            KEY)
        .stream()
        .findFirst();
  }

  /** Active keys of the given users; users without an active key are absent. */
  public List<UserPublicKey> findActive(List<Long> userIds) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    return jdbc.query(
        "SELECT "
            + COLUMNS
            + " FROM user_public_keys k WHERE k.user_id IN (:ids) AND k.retired_at IS NULL"
            + " ORDER BY k.user_id",
        Map.of("ids", userIds),
        KEY);
  }

  /** Highest key version the user has registered, or 0 when none. */
  public int findLatestVersion(long userId) {
    return jdbc.queryForObject(
        "SELECT coalesce(max(key_version), 0) FROM user_public_keys WHERE user_id = :userId",
        Map.of("userId", userId),
        Integer.class);
  }
}
