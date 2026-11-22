package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.dbValue;
import static com.chat.server.repository.SqlSupport.enumValue;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.enums.FriendRequestDirection;
import com.chat.common.enums.FriendRequestStatus;
import com.chat.server.model.FriendRequest;
import com.chat.server.model.FriendRequestView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code friend_requests}. At most one pending request exists per user pair. */
@Repository
public class FriendRequestRepository {
  private static final String COLUMNS =
      "r.friend_request_id, r.sender_user_id, r.receiver_user_id, r.status, r.created_at,"
          + " r.responded_at";

  private static final RowMapper<FriendRequest> REQUEST =
      (rs, n) ->
          new FriendRequest(
              rs.getLong("friend_request_id"),
              rs.getLong("sender_user_id"),
              rs.getLong("receiver_user_id"),
              enumValue(FriendRequestStatus.class, rs.getString("status")),
              timestamp(rs, "created_at"),
              timestamp(rs, "responded_at"));

  private static final RowMapper<FriendRequestView> VIEW =
      (rs, n) ->
          new FriendRequestView(
              rs.getLong("friend_request_id"),
              Rows.userBrief(rs, "s_"),
              Rows.userBrief(rs, "t_"),
              enumValue(FriendRequestStatus.class, rs.getString("status")),
              timestamp(rs, "created_at"),
              timestamp(rs, "responded_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public FriendRequestRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Inserts a pending request and returns its ID. */
  public long insertPending(long senderId, long receiverId) {
    return jdbc.queryForObject(
        "INSERT INTO friend_requests (sender_user_id, receiver_user_id)"
            + " VALUES (:sender, :receiver) RETURNING friend_request_id",
        Map.of("sender", senderId, "receiver", receiverId),
        Long.class);
  }

  public Optional<FriendRequest> findById(long friendRequestId) {
    return jdbc
        .query(
            "SELECT " + COLUMNS + " FROM friend_requests r WHERE r.friend_request_id = :id",
            Map.of("id", friendRequestId),
            REQUEST)
        .stream()
        .findFirst();
  }

  /** The pending request between the two users in either direction, if any. */
  public Optional<FriendRequest> findPendingBetween(long userA, long userB) {
    return jdbc
        .query(
            "SELECT "
                + COLUMNS
                + " FROM friend_requests r WHERE r.status = 'pending' AND ("
                + "(r.sender_user_id = :a AND r.receiver_user_id = :b)"
                + " OR (r.sender_user_id = :b AND r.receiver_user_id = :a))",
            Map.of("a", userA, "b", userB),
            REQUEST)
        .stream()
        .findFirst();
  }

  /**
   * Moves a pending request to {@code ACCEPTED}, {@code DECLINED} or {@code CANCELED}. Returns
   * false when the request is missing or no longer pending.
   */
  public boolean resolve(long friendRequestId, FriendRequestStatus newStatus) {
    if (newStatus == FriendRequestStatus.PENDING) {
      throw new IllegalArgumentException("A request cannot be resolved to PENDING");
    }
    return jdbc.update(
            "UPDATE friend_requests SET status = :status, responded_at = CURRENT_TIMESTAMP"
                + " WHERE friend_request_id = :id AND status = 'pending'",
            Map.of("id", friendRequestId, "status", dbValue(newStatus)))
        == 1;
  }

  /** Cancels every pending request between the two users, in either direction. */
  public int cancelPendingBetween(long userA, long userB) {
    return jdbc.update(
        "UPDATE friend_requests SET status = 'canceled', responded_at = CURRENT_TIMESTAMP"
            + " WHERE status = 'pending' AND ((sender_user_id = :a AND receiver_user_id = :b)"
            + " OR (sender_user_id = :b AND receiver_user_id = :a))",
        Map.of("a", userA, "b", userB));
  }

  /**
   * Requests involving {@code userId}, newest first. {@code direction} null includes both; {@code
   * query} matches the other user's username or full name.
   */
  public PageResult<FriendRequestView> findForUser(
      long userId,
      FriendRequestDirection direction,
      FriendRequestStatus status,
      String query,
      PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource("me", userId);
    List<String> conditions = new ArrayList<>();
    if (direction == FriendRequestDirection.INCOMING) {
      conditions.add("r.receiver_user_id = :me");
    } else if (direction == FriendRequestDirection.OUTGOING) {
      conditions.add("r.sender_user_id = :me");
    } else {
      conditions.add("(r.receiver_user_id = :me OR r.sender_user_id = :me)");
    }
    if (status != null) {
      conditions.add("r.status = :status");
      params.addValue("status", dbValue(status));
    }
    if (query != null) {
      conditions.add(
          "EXISTS (SELECT 1 FROM users o WHERE o.user_id = CASE WHEN r.sender_user_id = :me"
              + " THEN r.receiver_user_id ELSE r.sender_user_id END"
              + " AND (lower(o.username) LIKE :pattern ESCAPE '\\'"
              + " OR lower(o.full_name) LIKE :pattern ESCAPE '\\'))");
      params.addValue("pattern", containsPattern(query));
    }
    String where = " WHERE " + String.join(" AND ", conditions);
    long total =
        jdbc.queryForObject("SELECT count(*) FROM friend_requests r" + where, params, Long.class);
    List<FriendRequestView> items =
        jdbc.query(
            "SELECT r.friend_request_id, r.status, r.created_at, r.responded_at, "
                + Rows.userBriefColumns("s", "s_")
                + ", "
                + Rows.userBriefColumns("t", "t_")
                + " FROM friend_requests r"
                + " JOIN users s ON s.user_id = r.sender_user_id"
                + " JOIN users t ON t.user_id = r.receiver_user_id"
                + where
                + " ORDER BY r.created_at DESC, r.friend_request_id DESC"
                + limitOffset(page),
            params,
            VIEW);
    return SqlSupport.page(items, page, total);
  }
}
