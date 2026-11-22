package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.comparison;
import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.orderBy;
import static com.chat.server.repository.SqlSupport.rangeEndExclusive;
import static com.chat.server.repository.SqlSupport.rangeStart;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.server.model.ActivityStatisticRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code user_activity_logs}: activity events and Admin activity statistics. */
@Repository
public class UserActivityLogRepository {
  /** Sort fields accepted by {@link #findStatistics}. */
  static final Map<String, String> STATISTICS_SORT =
      Map.of("name", "lower(s.full_name)", "createdAt", "s.created_at");

  /**
   * Per-user activity in {@code [:from, :to)}: app opens, distinct users chatted with directly and
   * distinct groups chatted in. Every user with at least one event in the range is included,
   * including locked and soft-deleted accounts.
   */
  private static final String STATISTICS =
      "WITH counts AS (SELECT l.user_id,"
          + " count(*) FILTER (WHERE l.activity_type = 'app_open') AS app_opens,"
          + " count(DISTINCT l.counterpart_user_id_snapshot)"
          + " FILTER (WHERE l.activity_type = 'direct_chat') AS chatted_users,"
          + " count(DISTINCT l.conversation_id_snapshot)"
          + " FILTER (WHERE l.activity_type = 'group_chat') AS chatted_groups"
          + " FROM user_activity_logs l WHERE l.occurred_at >= :from AND l.occurred_at < :to"
          + " GROUP BY l.user_id)"
          + ", s AS (SELECT u.user_id, u.username, u.full_name, u.account_status, u.created_at,"
          + " c.app_opens, c.chatted_users, c.chatted_groups"
          + " FROM counts c JOIN users u ON u.user_id = c.user_id)";

  private static final RowMapper<ActivityStatisticRow> STATISTIC =
      (rs, n) ->
          new ActivityStatisticRow(
              Rows.userBrief(rs, ""),
              rs.getLong("app_opens"),
              rs.getLong("chatted_users"),
              rs.getLong("chatted_groups"));

  private final NamedParameterJdbcTemplate jdbc;

  public UserActivityLogRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Records that the user opened the application (logged in). */
  public void recordAppOpen(long userId) {
    jdbc.update(
        "INSERT INTO user_activity_logs (user_id, activity_type) VALUES (:user, 'app_open')",
        Map.of("user", userId));
  }

  /** Records that the user sent a message to {@code counterpartUserId} in a direct conversation. */
  public void recordDirectChat(long userId, long conversationId, long counterpartUserId) {
    jdbc.update(
        "INSERT INTO user_activity_logs (user_id, activity_type, conversation_id,"
            + " conversation_id_snapshot, counterpart_user_id_snapshot)"
            + " VALUES (:user, 'direct_chat', :conversation, :conversation, :counterpart)",
        Map.of("user", userId, "conversation", conversationId, "counterpart", counterpartUserId));
  }

  /** Records that the user sent a message in a group conversation. */
  public void recordGroupChat(long userId, long conversationId) {
    jdbc.update(
        "INSERT INTO user_activity_logs (user_id, activity_type, conversation_id,"
            + " conversation_id_snapshot) VALUES (:user, 'group_chat', :conversation,"
            + " :conversation)",
        Map.of("user", userId, "conversation", conversationId));
  }

  /**
   * Users active in the range with their counts. {@code nameQuery} matches username or full name;
   * each numeric filter compares one count. Sort fields: {@code name}, {@code createdAt}.
   */
  public PageResult<ActivityStatisticRow> findStatistics(
      DateRange range,
      String nameQuery,
      NumericFilter appOpens,
      NumericFilter chattedUsers,
      NumericFilter chattedGroups,
      SortSpec sort,
      PageRequest page) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("from", rangeStart(range))
            .addValue("to", rangeEndExclusive(range));
    List<String> conditions = new ArrayList<>();
    if (nameQuery != null) {
      conditions.add(
          "(lower(s.username) LIKE :pattern ESCAPE '\\'"
              + " OR lower(s.full_name) LIKE :pattern ESCAPE '\\')");
      params.addValue("pattern", containsPattern(nameQuery));
    }
    addFilter(conditions, params, "s.app_opens", "appOpens", appOpens);
    addFilter(conditions, params, "s.chatted_users", "chattedUsers", chattedUsers);
    addFilter(conditions, params, "s.chatted_groups", "chattedGroups", chattedGroups);
    String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    long total =
        jdbc.queryForObject(STATISTICS + " SELECT count(*) FROM s" + where, params, Long.class);
    List<ActivityStatisticRow> items =
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

  /**
   * Distinct users who opened the application in each month of {@code year}; index 0 is January.
   */
  public long[] countMonthlyActiveUsers(int year) {
    long[] counts = new long[12];
    jdbc.query(
        "SELECT extract(MONTH FROM occurred_at AT TIME ZONE :zone)::int AS month,"
            + " count(DISTINCT user_id) AS total FROM user_activity_logs"
            + " WHERE activity_type = 'app_open' AND occurred_at >= :from AND occurred_at < :to"
            + " GROUP BY 1",
        SqlSupport.yearParams(year),
        rs -> {
          counts[rs.getInt("month") - 1] = rs.getLong("total");
        });
    return counts;
  }

  private static void addFilter(
      List<String> conditions,
      MapSqlParameterSource params,
      String column,
      String name,
      NumericFilter filter) {
    if (filter != null) {
      conditions.add(column + " " + comparison(filter) + " :" + name);
      params.addValue(name, filter.value());
    }
  }
}
