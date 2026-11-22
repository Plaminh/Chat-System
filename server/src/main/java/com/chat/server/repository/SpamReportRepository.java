package com.chat.server.repository;

import static com.chat.server.repository.SqlSupport.containsPattern;
import static com.chat.server.repository.SqlSupport.dbValue;
import static com.chat.server.repository.SqlSupport.enumValue;
import static com.chat.server.repository.SqlSupport.limitOffset;
import static com.chat.server.repository.SqlSupport.nullableLong;
import static com.chat.server.repository.SqlSupport.orderBy;
import static com.chat.server.repository.SqlSupport.rangeEndExclusive;
import static com.chat.server.repository.SqlSupport.rangeStart;
import static com.chat.server.repository.SqlSupport.timestamp;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.SpamReportStatus;
import com.chat.server.model.SpamReport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Data access for {@code spam_reports}. */
@Repository
public class SpamReportRepository {
  /** Sort fields accepted by {@link #findForAdmin}; {@code username} is the reported username. */
  static final Map<String, String> SORT =
      Map.of("createdAt", "r.created_at", "username", "lower(p.username)");

  private static final String SELECT =
      "SELECT r.spam_report_id, r.message_id, r.reason, r.review_status, r.resolution_note,"
          + " r.created_at, r.resolved_at, "
          + Rows.userBriefColumns("a", "a_")
          + ", "
          + Rows.userBriefColumns("p", "p_")
          + ", "
          + Rows.userBriefColumns("v", "v_");

  private static final String FROM =
      " FROM spam_reports r"
          + " LEFT JOIN users a ON a.user_id = r.reporter_user_id"
          + " LEFT JOIN users p ON p.user_id = r.reported_user_id"
          + " LEFT JOIN users v ON v.user_id = r.reviewed_by";

  private static final RowMapper<SpamReport> REPORT =
      (rs, n) ->
          new SpamReport(
              rs.getLong("spam_report_id"),
              Rows.userBrief(rs, "a_"),
              Rows.userBrief(rs, "p_"),
              nullableLong(rs, "message_id"),
              rs.getString("reason"),
              enumValue(SpamReportStatus.class, rs.getString("review_status")),
              Rows.userBrief(rs, "v_"),
              rs.getString("resolution_note"),
              timestamp(rs, "created_at"),
              timestamp(rs, "resolved_at"));

  private final NamedParameterJdbcTemplate jdbc;

  public SpamReportRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  /** Stores a pending report and returns its ID. */
  public long insert(long reporterUserId, long reportedUserId, Long messageId, String reason) {
    return jdbc.queryForObject(
        "INSERT INTO spam_reports (reporter_user_id, reported_user_id, message_id, reason)"
            + " VALUES (:reporter, :reported, :message, :reason) RETURNING spam_report_id",
        new MapSqlParameterSource()
            .addValue("reporter", reporterUserId)
            .addValue("reported", reportedUserId)
            .addValue("message", messageId)
            .addValue("reason", reason),
        Long.class);
  }

  public Optional<SpamReport> findById(long spamReportId) {
    return jdbc
        .query(SELECT + FROM + " WHERE r.spam_report_id = :id", Map.of("id", spamReportId), REPORT)
        .stream()
        .findFirst();
  }

  /**
   * Updates the review state. {@code RESOLVED} and {@code DISMISSED} set the resolution time;
   * {@code PENDING} and {@code REVIEWING} clear it. Returns false when the report is missing.
   */
  public boolean updateReview(
      long spamReportId, SpamReportStatus status, long reviewerUserId, String resolutionNote) {
    boolean closed = status == SpamReportStatus.RESOLVED || status == SpamReportStatus.DISMISSED;
    return jdbc.update(
            "UPDATE spam_reports SET review_status = :status, reviewed_by = :reviewer,"
                + " resolution_note = :note,"
                + (closed ? " resolved_at = CURRENT_TIMESTAMP" : " resolved_at = NULL")
                + " WHERE spam_report_id = :id",
            new MapSqlParameterSource()
                .addValue("id", spamReportId)
                .addValue("status", dbValue(status))
                .addValue("reviewer", reviewerUserId)
                .addValue("note", resolutionNote))
        == 1;
  }

  /**
   * Admin report list. Filters: creation date range, reported username (substring) and status. Sort
   * fields: {@code createdAt}, {@code username}.
   */
  public PageResult<SpamReport> findForAdmin(
      DateRange dateRange,
      String reportedUsername,
      SpamReportStatus status,
      SortSpec sort,
      PageRequest page) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    List<String> conditions = new ArrayList<>();
    if (dateRange != null) {
      conditions.add("r.created_at >= :from AND r.created_at < :to");
      params.addValue("from", rangeStart(dateRange));
      params.addValue("to", rangeEndExclusive(dateRange));
    }
    if (reportedUsername != null && !reportedUsername.isBlank()) {
      conditions.add("lower(p.username) LIKE :pattern ESCAPE '\\'");
      params.addValue("pattern", containsPattern(reportedUsername.strip()));
    }
    if (status != null) {
      conditions.add("r.review_status = :status");
      params.addValue("status", dbValue(status));
    }
    String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    long total = jdbc.queryForObject("SELECT count(*)" + FROM + where, params, Long.class);
    List<SpamReport> items =
        jdbc.query(
            SELECT + FROM + where + orderBy(sort, SORT, "r.spam_report_id") + limitOffset(page),
            params,
            REPORT);
    return SqlSupport.page(items, page, total);
  }
}
