package com.chat.server.repository;

import com.chat.server.model.UserBrief;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for the directed {@code user_blocks} table. */
@Repository
public class UserBlockRepository {
  private final NamedParameterJdbcTemplate jdbc;

  public UserBlockRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Records that {@code blockerId} blocks {@code blockedId}; false when the block already exists.
   */
  public boolean block(long blockerId, long blockedId) {
    return jdbc.update(
            "INSERT INTO user_blocks (blocker_user_id, blocked_user_id) VALUES (:blocker, :blocked)"
                + " ON CONFLICT ON CONSTRAINT uq_user_blocks_pair DO NOTHING",
            Map.of("blocker", blockerId, "blocked", blockedId))
        == 1;
  }

  /** Removes the block; false when it did not exist. */
  public boolean unblock(long blockerId, long blockedId) {
    return jdbc.update(
            "DELETE FROM user_blocks WHERE blocker_user_id = :blocker AND blocked_user_id ="
                + " :blocked",
            Map.of("blocker", blockerId, "blocked", blockedId))
        == 1;
  }

  /** True when {@code blockerId} has blocked {@code blockedId}. */
  public boolean isBlocked(long blockerId, long blockedId) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM user_blocks"
                + " WHERE blocker_user_id = :blocker AND blocked_user_id = :blocked)",
            Map.of("blocker", blockerId, "blocked", blockedId),
            Boolean.class));
  }

  /** True when either user has blocked the other. */
  public boolean isBlockedEitherWay(long userA, long userB) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM user_blocks"
                + " WHERE (blocker_user_id = :a AND blocked_user_id = :b)"
                + " OR (blocker_user_id = :b AND blocked_user_id = :a))",
            Map.of("a", userA, "b", userB),
            Boolean.class));
  }

  /** Users blocked by {@code blockerId}, newest block first. */
  public List<UserBrief> findBlockedBy(long blockerId) {
    return jdbc.query(
        "SELECT "
            + Rows.userBriefColumns("u", "u_")
            + " FROM user_blocks b JOIN users u ON u.user_id = b.blocked_user_id WHERE"
            + " b.blocker_user_id = :blocker ORDER BY b.created_at DESC, b.user_block_id DESC",
        Map.of("blocker", blockerId),
        (rs, n) -> Rows.userBrief(rs, "u_"));
  }
}
