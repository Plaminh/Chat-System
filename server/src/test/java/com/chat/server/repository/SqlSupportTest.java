package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.SortDirection;
import com.chat.server.exception.InvalidSortFieldException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SqlSupportTest {
  @Test
  void dateRangesUseVietnamTimeRegardlessOfTheMachineZone() {
    DateRange range = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    ZoneOffset vietnam = ZoneOffset.ofHours(7);

    assertEquals(OffsetDateTime.of(2026, 11, 1, 0, 0, 0, 0, vietnam), SqlSupport.rangeStart(range));
    assertEquals(
        OffsetDateTime.of(2026, 12, 1, 0, 0, 0, 0, vietnam), SqlSupport.rangeEndExclusive(range));
    assertEquals("Asia/Ho_Chi_Minh", SqlSupport.zoneId());
    assertEquals(
        OffsetDateTime.of(2025, 12, 31, 17, 0, 0, 0, ZoneOffset.UTC).toInstant(),
        ((OffsetDateTime) SqlSupport.yearParams(2026).getValue("from")).toInstant(),
        "1 January in Vietnam starts at 17:00 UTC the day before");
  }

  @Test
  void orderByUsesOnlyAllowListedColumns() {
    Map<String, String> allowed = Map.of("name", "lower(u.full_name)", "createdAt", "u.created_at");

    assertEquals(
        " ORDER BY u.created_at DESC, u.user_id DESC",
        SqlSupport.orderBy(new SortSpec("createdAt", SortDirection.DESC), allowed, "u.user_id"));
    InvalidSortFieldException error =
        assertThrows(
            InvalidSortFieldException.class,
            () ->
                SqlSupport.orderBy(new SortSpec("email", SortDirection.ASC), allowed, "u.user_id"));
    assertEquals("email", error.field());
    assertEquals(List.of("createdAt", "name"), error.allowedFields());
  }

  @Test
  void containsPatternEscapesLikeWildcards() {
    assertEquals("%50\\%\\_off\\\\%", SqlSupport.containsPattern("50%_OFF\\"));
  }
}
