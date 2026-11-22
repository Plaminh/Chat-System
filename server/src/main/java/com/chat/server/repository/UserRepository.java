package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.date;
import static com.chat.server.repository.SqlSupport.dbValue;
import static com.chat.server.repository.SqlSupport.enumValue;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.orderBy;
import static com.chat.server.repository.SqlSupport.rangeEndExclusive;
import static com.chat.server.repository.SqlSupport.rangeStart;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import com.chat.server.model.NewUser;
import com.chat.server.model.User;
import com.chat.server.model.UserBrief;
import com.chat.server.model.UserProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code users}. Usernames and emails are compared case-insensitively. */
@Repository
public class UserRepository {
  /** Sort fields accepted by {@link #findForAdmin}: name (full name) and creation time. */
  static final Map<String, String> ADMIN_SORT =
      Map.of("name", "lower(u.full_name)", "createdAt", "u.created_at");

  private static final String COLUMNS =
      "u.user_id, u.username, u.password_hash, u.email, u.full_name, u.address, u.date_of_birth,"
          + " u.gender, u.role, u.account_status, u.locked_at, u.deleted_at,"
          + " u.password_changed_at, u.created_at, u.updated_at";

  private static final RowMapper<User> USER =
      (rs, n) ->
          new User(
              rs.getLong("user_id"),
              rs.getString("username"),
              rs.getString("password_hash"),
              rs.getString("email"),
              rs.getString("full_name"),
              rs.getString("address"),
              date(rs, "date_of_birth"),
              enumValue(Gender.class, rs.getString("gender")),
              enumValue(UserRole.class, rs.getString("role")),
              enumValue(AccountStatus.class, rs.getString("account_status")),
              timestamp(rs, "locked_at"),
              timestamp(rs, "deleted_at"),
              timestamp(rs, "password_changed_at"),
              timestamp(rs, "created_at"),
              timestamp(rs, "updated_at"));

  private static final RowMapper<UserBrief> BRIEF = (rs, n) -> Rows.userBrief(rs, "u_");

  private final NamedParameterJdbcTemplate jdbc;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Inserts an active account and returns its generated ID. */
  public long insert(NewUser user) {
    return jdbc.queryForObject(
        "INSERT INTO users (username, password_hash, email, full_name, address, date_of_birth,"
            + " gender, role) VALUES (:username, :passwordHash, :email, :fullName, :address,"
            + " :dateOfBirth, :gender, :role) RETURNING user_id",
        new MapSqlParameterSource()
            .addValue("username", user.username())
            .addValue("passwordHash", user.passwordHash())
            .addValue("email", user.email())
            .addValue("fullName", user.fullName())
            .addValue("address", user.address())
            .addValue("dateOfBirth", user.dateOfBirth())
            .addValue("gender", dbValue(user.gender()))
            .addValue("role", dbValue(user.role())),
        Long.class);
  }

  public Optional<User> findById(long userId) {
    return jdbc
        .query(
            "SELECT " + COLUMNS + " FROM users u WHERE u.user_id = :id", Map.of("id", userId), USER)
        .stream()
        .findFirst();
  }

  public Optional<User> findByUsername(String username) {
    return jdbc
        .query(
            "SELECT " + COLUMNS + " FROM users u WHERE lower(u.username) = lower(:username)",
            Map.of("username", username),
            USER)
        .stream()
        .findFirst();
  }

  public Optional<User> findByEmail(String email) {
    return jdbc
        .query(
            "SELECT " + COLUMNS + " FROM users u WHERE lower(u.email) = lower(:email)",
            Map.of("email", email),
            USER)
        .stream()
        .findFirst();
  }

  public boolean existsByUsername(String username) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM users WHERE lower(username) = lower(:username))",
            Map.of("username", username),
            Boolean.class));
  }

  /** True when another account than {@code exceptUserId} uses the email; pass 0 to check all. */
  public boolean existsByEmail(String email, long exceptUserId) {
    return Boolean.TRUE.equals(
        jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower(:email)"
                + " AND user_id <> :exceptUserId)",
            Map.of("email", email, "exceptUserId", exceptUserId),
            Boolean.class));
  }

  /** Updates the profile columns; returns false when the user does not exist. */
  public boolean updateProfile(long userId, UserProfile profile) {
    return jdbc.update(
            "UPDATE users SET full_name = :fullName, email = :email, address = :address,"
                + " date_of_birth = :dateOfBirth, gender = :gender,"
                + " updated_at = CURRENT_TIMESTAMP WHERE user_id = :id",
            profileParams(userId, profile))
        == 1;
  }

  /** Updates the profile and role in one statement (Admin edit). */
  public boolean updateProfileAndRole(long userId, UserProfile profile, UserRole role) {
    return jdbc.update(
            "UPDATE users SET full_name = :fullName, email = :email, address = :address,"
                + " date_of_birth = :dateOfBirth, gender = :gender, role = :role,"
                + " updated_at = CURRENT_TIMESTAMP WHERE user_id = :id",
            profileParams(userId, profile).addValue("role", dbValue(role)))
        == 1;
  }

  /** Stores a new BCrypt hash and records the change time. */
  public boolean updatePasswordHash(long userId, String passwordHash) {
    return jdbc.update(
            "UPDATE users SET password_hash = :hash, password_changed_at = CURRENT_TIMESTAMP,"
                + " updated_at = CURRENT_TIMESTAMP WHERE user_id = :id",
            Map.of("id", userId, "hash", passwordHash))
        == 1;
  }

  /** Locks an active account; returns false when the account is missing or not active. */
  public boolean lock(long userId) {
    return jdbc.update(
            "UPDATE users SET account_status = 'locked', locked_at = CURRENT_TIMESTAMP, updated_at"
                + " = CURRENT_TIMESTAMP WHERE user_id = :id AND account_status = 'active'",
            Map.of("id", userId))
        == 1;
  }

  /** Unlocks a locked account; returns false when the account is missing or not locked. */
  public boolean unlock(long userId) {
    return jdbc.update(
            "UPDATE users SET account_status = 'active', locked_at = NULL, updated_at ="
                + " CURRENT_TIMESTAMP WHERE user_id = :id AND account_status = 'locked'",
            Map.of("id", userId))
        == 1;
  }

  /**
   * Soft-deletes an account; deletion is terminal. Returns false when already deleted or missing.
   */
  public boolean softDelete(long userId) {
    return jdbc.update(
            "UPDATE users SET account_status = 'deleted', locked_at = NULL,"
                + " deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP"
                + " WHERE user_id = :id AND account_status <> 'deleted'",
            Map.of("id", userId))
        == 1;
  }

  /**
   * Admin user list: optional username/full-name query, status and creation-date range. Sort
   * fields: {@code name}, {@code createdAt}.
   */
  public PageResult<User> findForAdmin(
      String query,
      AccountStatus status,
      DateRange createdAtRange,
      SortSpec sort,
      PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    List<String> conditions = new ArrayList<>();
    if (query != null) {
      conditions.add(
          "(lower(u.username) LIKE :pattern ESCAPE '\\' OR lower(u.full_name) LIKE :pattern"
              + " ESCAPE '\\')");
      params.addValue("pattern", containsPattern(query));
    }
    if (status != null) {
      conditions.add("u.account_status = :status");
      params.addValue("status", dbValue(status));
    }
    if (createdAtRange != null) {
      conditions.add("u.created_at >= :from AND u.created_at < :to");
      params.addValue("from", rangeStart(createdAtRange));
      params.addValue("to", rangeEndExclusive(createdAtRange));
    }
    String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    long total = jdbc.queryForObject("SELECT count(*) FROM users u" + where, params, Long.class);
    List<User> items =
        jdbc.query(
            "SELECT "
                + COLUMNS
                + " FROM users u"
                + where
                + orderBy(sort, ADMIN_SORT, "u.user_id")
                + limitOffset(page),
            params,
            USER);
    return SqlSupport.page(items, page, total);
  }

  /**
   * User search by username or full name. Excludes the searcher, deleted accounts, and accounts
   * blocked in either direction.
   */
  public PageResult<UserBrief> search(long searcherId, String query, PageRequest page) {
    String from =
        " FROM users u WHERE u.user_id <> :searcher AND u.account_status <> 'deleted'"
            + " AND (lower(u.username) LIKE :pattern ESCAPE '\\'"
            + " OR lower(u.full_name) LIKE :pattern ESCAPE '\\')"
            + " AND NOT EXISTS (SELECT 1 FROM user_blocks b"
            + " WHERE (b.blocker_user_id = :searcher AND b.blocked_user_id = u.user_id)"
            + " OR (b.blocker_user_id = u.user_id AND b.blocked_user_id = :searcher))";
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("searcher", searcherId)
            .addValue("pattern", containsPattern(query));
    long total = jdbc.queryForObject("SELECT count(*)" + from, params, Long.class);
    List<UserBrief> items =
        jdbc.query(
            "SELECT "
                + Rows.userBriefColumns("u", "u_")
                + from
                + " ORDER BY lower(u.full_name), u.user_id"
                + limitOffset(page),
            params,
            BRIEF);
    return SqlSupport.page(items, page, total);
  }

  /** Light projections for the given IDs, in no particular order. */
  public List<UserBrief> findBriefsByIds(List<Long> userIds) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    return jdbc.query(
        "SELECT " + Rows.userBriefColumns("u", "u_") + " FROM users u WHERE u.user_id IN (:ids)",
        Map.of("ids", userIds),
        BRIEF);
  }

  /** Number of accounts created in the date range. */
  public long countRegistrations(DateRange range) {
    return jdbc.queryForObject(
        "SELECT count(*) FROM users WHERE created_at >= :from AND created_at < :to",
        Map.of("from", rangeStart(range), "to", rangeEndExclusive(range)),
        Long.class);
  }

  /** New accounts per month of {@code year}; index 0 is January. Months without data are 0. */
  public long[] countMonthlyRegistrations(int year) {
    long[] counts = new long[12];
    jdbc.query(
        "SELECT extract(MONTH FROM created_at AT TIME ZONE :zone)::int AS month, count(*) AS total"
            + " FROM users WHERE created_at >= :from AND created_at < :to GROUP BY 1",
        SqlSupport.yearParams(year),
        rs -> {
          counts[rs.getInt("month") - 1] = rs.getLong("total");
        });
    return counts;
  }

  private static MapSqlParameterSource profileParams(long userId, UserProfile profile) {
    return new MapSqlParameterSource()
        .addValue("id", userId)
        .addValue("fullName", profile.fullName())
        .addValue("email", profile.email())
        .addValue("address", profile.address())
        .addValue("dateOfBirth", profile.dateOfBirth())
        .addValue("gender", dbValue(profile.gender()));
  }
}
