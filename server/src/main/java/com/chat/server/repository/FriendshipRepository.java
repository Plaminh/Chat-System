package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.comparison;
import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.orderBy;

import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.server.model.FriendStatisticRow;
import com.chat.server.model.UserBrief;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Data access for {@code friendships}. Each pair is stored once with {@code user_low_id <
 * user_high_id}; callers pass the two users in any order.
 */
@Repository
public class FriendshipRepository {
  /** Sort fields accepted by {@link #findStatistics}. */
  static final Map<String, String> STATISTICS_SORT =
      Map.of("name", "lower(s.full_name)", "createdAt", "s.created_at");

  /** Both directions of every friendship as rows {@code (a, b)}. */
  private static final String EDGES =
      "edges AS (SELECT user_low_id AS a, user_high_id AS b FROM friendships"
          + " UNION ALL SELECT user_high_id, user_low_id FROM friendships)";

  /**
   * Per-user direct friend count and distinct friends-of-friends count. Friends of friends exclude
   * the user and the user's direct friends.
   */
  private static final String STATISTICS =
      "WITH "
          + EDGES
          + ", direct AS (SELECT a AS user_id, count(*) AS total FROM edges GROUP BY a)"
          + ", second AS (SELECT e1.a AS user_id, count(DISTINCT e2.b) AS total"
          + " FROM edges e1 JOIN edges e2 ON e2.a = e1.b"
          + " WHERE e2.b <> e1.a"
          + " AND NOT EXISTS (SELECT 1 FROM edges e3 WHERE e3.a = e1.a AND e3.b = e2.b)"
          + " GROUP BY e1.a)"
          + ", s AS (SELECT u.user_id, u.username, u.full_name, u.account_status, u.created_at,"
          + " coalesce(d.total, 0) AS direct_friends, coalesce(f.total, 0) AS friends_of_friends"
          + " FROM users u LEFT JOIN direct d ON d.user_id = u.user_id"
          + " LEFT JOIN second f ON f.user_id = u.user_id)";

  private static final RowMapper<FriendStatisticRow> STATISTIC =
      (rs, n) ->
          new FriendStatisticRow(
              Rows.userBrief(rs, ""),
              rs.getLong("direct_friends"),
              rs.getLong("friends_of_friends"));

  private final NamedParameterJdbcTemplate jdbc;

  public FriendshipRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /**
   * Stores the friendship; returns false when the pair is already friends.
   *
   * @param requestId the accepted request that created it, or null
   */
  public boolean insert(long userA, long userB, Long requestId) {
    return jdbc.update(
            "INSERT INTO friendships (user_low_id, user_high_id, established_by_request_id)"
                + " VALUES (:low, :high, :requestId)"
                + " ON CONFLICT ON CONSTRAINT uq_friendships_pair DO NOTHING",
            pair(userA, userB).addValue("requestId", requestId))
        == 1;
  }

  /** Removes the friendship; false when the pair were not friends. */
  public boolean delete(long userA, long userB) {
    return jdbc.update(
            "DELETE FROM friendships WHERE user_low_id = :low AND user_high_id = :high",
            pair(userA, userB))
        == 1;
  }

  public boolean areFriends(long userA, long userB) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM friendships"
                + " WHERE user_low_id = :low AND user_high_id = :high)",
            pair(userA, userB),
            Boolean.class));
  }

  /** IDs of all direct friends, e.g. to combine with the runtime online-user list. */
  public List<Long> findFriendIds(long userId) {
    return jdbc.queryForList(
        "SELECT CASE WHEN user_low_id = :me THEN user_high_id ELSE user_low_id END"
            + " FROM friendships WHERE user_low_id = :me OR user_high_id = :me",
        Map.of("me", userId),
        Long.class);
  }

  /**
   * Direct friends ordered by full name, optionally filtered by username or full name. Deleted
   * accounts are excluded.
   */
  public PageResult<UserBrief> findFriends(long userId, String query, PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource("me", userId);
    String from =
        " FROM friendships f JOIN users u ON u.user_id = CASE WHEN f.user_low_id = :me"
            + " THEN f.user_high_id ELSE f.user_low_id END"
            + " WHERE (f.user_low_id = :me OR f.user_high_id = :me)"
            + " AND u.account_status <> 'deleted'";
    if (query != null) {
      from +=
          " AND (lower(u.username) LIKE :pattern ESCAPE '\\'"
              + " OR lower(u.full_name) LIKE :pattern ESCAPE '\\')";
      params.addValue("pattern", containsPattern(query));
    }
    long total = jdbc.queryForObject("SELECT count(*)" + from, params, Long.class);
    List<UserBrief> items =
        jdbc.query(
            "SELECT "
                + Rows.userBriefColumns("u", "u_")
                + from
                + " ORDER BY lower(u.full_name), u.user_id"
                + limitOffset(page),
            params,
            (rs, n) -> Rows.userBrief(rs, "u_"));
    return SqlSupport.page(items, page, total);
  }

  /** All direct friends ordered by full name, including deleted accounts (Admin view). */
  public List<UserBrief> findAllFriends(long userId) {
    return jdbc.query(
        "SELECT "
            + Rows.userBriefColumns("u", "u_")
            + " FROM friendships f JOIN users u ON u.user_id = CASE WHEN f.user_low_id = :me"
            + " THEN f.user_high_id ELSE f.user_low_id END"
            + " WHERE f.user_low_id = :me OR f.user_high_id = :me"
            + " ORDER BY lower(u.full_name), u.user_id",
        Map.of("me", userId),
        (rs, n) -> Rows.userBrief(rs, "u_"));
  }

  /** Number of distinct friends of the user's friends, excluding the user and direct friends. */
  public long countFriendsOfFriends(long userId) {
    return jdbc.queryForObject(
        "WITH "
            + EDGES
            + " SELECT count(DISTINCT e2.b) FROM edges e1 JOIN edges e2 ON e2.a = e1.b"
            + " WHERE e1.a = :me AND e2.b <> :me"
            + " AND NOT EXISTS (SELECT 1 FROM edges e3 WHERE e3.a = :me AND e3.b = e2.b)",
        Map.of("me", userId),
        Long.class);
  }

  /**
   * Friend counts for every account, including locked and soft-deleted ones. {@code nameQuery}
   * matches username or full name; numeric filters compare the direct and friends-of-friends
   * counts. Sort fields: {@code name}, {@code createdAt}.
   */
  public PageResult<FriendStatisticRow> findStatistics(
      String nameQuery,
      NumericFilter directFriends,
      NumericFilter friendsOfFriends,
      SortSpec sort,
      PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    List<String> conditions = new ArrayList<>();
    if (nameQuery != null) {
      conditions.add(
          "(lower(s.username) LIKE :pattern ESCAPE '\\'"
              + " OR lower(s.full_name) LIKE :pattern ESCAPE '\\')");
      params.addValue("pattern", containsPattern(nameQuery));
    }
    if (directFriends != null) {
      conditions.add("s.direct_friends " + comparison(directFriends) + " :direct");
      params.addValue("direct", directFriends.value());
    }
    if (friendsOfFriends != null) {
      conditions.add("s.friends_of_friends " + comparison(friendsOfFriends) + " :second");
      params.addValue("second", friendsOfFriends.value());
    }
    String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    long total =
        jdbc.queryForObject(STATISTICS + " SELECT count(*) FROM s" + where, params, Long.class);
    List<FriendStatisticRow> items =
        jdbc.query(
            STATISTICS
                + " SELECT s.* FROM s"
                + where
                + orderBy(sort, STATISTICS_SORT, "s.user_id")
                + limitOffset(page),
            params,
            STATISTIC);
    return SqlSupport.page(items, page, total);
  }

  private static MapSqlParameterSource pair(long userA, long userB) {
    if (userA == userB) {
      throw new IllegalArgumentException("A user cannot be paired with themselves");
    }
    return new MapSqlParameterSource()
        .addValue("low", Math.min(userA, userB))
        .addValue("high", Math.max(userA, userB));
  }
}
