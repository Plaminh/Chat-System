package com.chat.server.repository;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.PageRequest;
import com.chat.common.dto.common.PageResult;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.SortDirection;
import com.chat.server.exception.InvalidSortFieldException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/** Shared helpers for the Spring JDBC repositories. */
final class SqlSupport {
  private SqlSupport() {}

  /** Converts a Java enum to the lowercase value stored by the schema CHECK constraints. */
  static String dbValue(Enum<?> value) {
    return value == null ? null : value.name().toLowerCase(Locale.ROOT);
  }

  /** Converts a lowercase schema value back to the matching Java enum. */
  static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
    return value == null ? null : Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
  }

  static OffsetDateTime timestamp(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, OffsetDateTime.class);
  }

  static LocalDate date(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, LocalDate.class);
  }

  static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }

  static Integer nullableInt(ResultSet rs, String column) throws SQLException {
    int value = rs.getInt(column);
    return rs.wasNull() ? null : value;
  }

  /**
   * Builds a case-insensitive LIKE pattern for a literal substring. Escapes backslash, percent and
   * underscore so user text never acts as a wildcard; use it with {@code ESCAPE '\'}.
   */
  static String containsPattern(String text) {
    String escaped =
        text.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    return "%" + escaped + "%";
  }

  /**
   * Vietnam time. Calendar dates and months chosen in the clients (date filters, monthly charts)
   * are always interpreted in this zone, whatever the server machine's zone is.
   */
  static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  static String zoneId() {
    return ZONE.getId();
  }

  /** Start of the first day of the range in Vietnam time, inclusive. */
  static OffsetDateTime rangeStart(DateRange range) {
    return range.from().atStartOfDay(ZONE).toOffsetDateTime();
  }

  /** Start of the day after the last day of the range in Vietnam time, exclusive. */
  static OffsetDateTime rangeEndExclusive(DateRange range) {
    return range.to().plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
  }

  /**
   * Resolves an ORDER BY expression from a client sort field through a fixed allow-list, so client
   * text is never concatenated into SQL.
   *
   * @throws InvalidSortFieldException when the field is not allowed
   */
  static String orderBy(SortSpec sort, Map<String, String> allowedColumns, String tieBreaker) {
    String column = allowedColumns.get(sort.field());
    if (column == null) {
      throw new InvalidSortFieldException(
          sort.field(), allowedColumns.keySet().stream().sorted().toList());
    }
    String direction = sort.direction() == SortDirection.DESC ? "DESC" : "ASC";
    return " ORDER BY " + column + " " + direction + ", " + tieBreaker + " " + direction;
  }

  /** Returns the SQL comparison operator for a numeric filter. */
  static String comparison(NumericFilter filter) {
    return switch (filter.operator()) {
      case EQ -> "=";
      case GT -> ">";
      case LT -> "<";
    };
  }

  static String limitOffset(PageRequest page) {
    return " LIMIT " + page.size() + " OFFSET " + ((long) page.page() * page.size());
  }

  static <T> PageResult<T> page(List<T> items, PageRequest page, long totalItems) {
    int totalPages = (int) ((totalItems + page.size() - 1) / page.size());
    return new PageResult<>(items, page.page(), page.size(), totalItems, totalPages);
  }

  /** Parameters {@code zone}, {@code from} and {@code to} covering one calendar year. */
  static MapSqlParameterSource yearParams(int year) {
    DateRange range = new DateRange(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    return new MapSqlParameterSource()
        .addValue("zone", zoneId())
        .addValue("from", rangeStart(range))
        .addValue("to", rangeEndExclusive(range));
  }
}
