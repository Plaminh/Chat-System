package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.orderBy;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.server.model.LoginHistoryRecord;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code login_history}. */
@Repository
public class LoginHistoryRepository {
  /** The only sort field allowed for login history. */
  static final Map<String, String> SORT = Map.of("loggedInAt", "h.logged_in_at");

  private static final RowMapper<LoginHistoryRecord> RECORD =
      (rs, n) ->
          new LoginHistoryRecord(
              rs.getLong("login_history_id"),
              rs.getLong("user_id"),
              rs.getString("username"),
              rs.getString("full_name"),
              timestamp(rs, "logged_in_at"),
              timestamp(rs, "logged_out_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public LoginHistoryRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Records a successful login and returns the history row ID. */
  public long recordLogin(long userId) {
    return jdbc.queryForObject(
        "INSERT INTO login_history (user_id) VALUES (:userId) RETURNING login_history_id",
        Map.of("userId", userId),
        Long.class);
  }

  /** Sets the logout time once; returns false when the row is missing or already closed. */
  public boolean recordLogout(long loginHistoryId) {
    return jdbc.update(
            "UPDATE login_history SET logged_out_at = CURRENT_TIMESTAMP"
                + " WHERE login_history_id = :id AND logged_out_at IS NULL",
            Map.of("id", loginHistoryId))
        == 1;
  }

  /**
   * Login history joined with username and full name. {@code userId} null returns the global
   * history. Sort field: {@code loggedInAt}.
   */
  public PageResult<LoginHistoryRecord> find(Long userId, SortSpec sort, PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    String where = "";
    if (userId != null) {
      where = " WHERE h.user_id = :userId";
      params.addValue("userId", userId);
    }
    long total =
        jdbc.queryForObject("SELECT count(*) FROM login_history h" + where, params, Long.class);
    List<LoginHistoryRecord> items =
        jdbc.query(
            "SELECT h.login_history_id, h.user_id, u.username, u.full_name, h.logged_in_at,"
                + " h.logged_out_at FROM login_history h JOIN users u ON u.user_id = h.user_id"
                + where
                + orderBy(sort, SORT, "h.login_history_id")
                + limitOffset(page),
            params,
            RECORD);
    return SqlSupport.page(items, page, total);
  }
}
