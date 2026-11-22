package com.chat.server.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.chat.common.dto.common.DateRange;
import com.chat.common.dto.common.NumericFilter;
import com.chat.common.dto.common.SortSpec;
import com.chat.common.enums.ComparisonOperator;
import com.chat.common.enums.GroupRole;
import com.chat.common.enums.SortDirection;
import com.chat.common.enums.SpamReportStatus;
import com.chat.common.enums.UserRole;
import com.chat.server.model.ActivityStatisticRow;
import com.chat.server.model.SpamReport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ModerationActivityRepositoryTest extends RepositoryTestSupport {
  @Autowired SpamReportRepository spamReports;
  @Autowired UserActivityLogRepository activity;
  @Autowired ConversationRepository conversations;
  @Autowired ConversationMemberRepository members;

  @Test
  void storesListsAndReviewsSpamReports() {
    inRollback(
        () -> {
          long reporter = newUser("aSpamReporter");
          long first = newUser("aSpamTargetA");
          long second = newUser("aSpamTargetB");
          long admin = newUser("aSpamAdmin", UserRole.ADMIN);
          long older = spamReports.insert(reporter, first, null, "quang cao");
          long newer = spamReports.insert(reporter, second, null, "lam phien");
          shiftTime("spam_reports", "created_at", "spam_report_id", older, -5);

          SortSpec newestFirst = new SortSpec("createdAt", SortDirection.DESC);
          assertEquals(
              List.of(newer, older),
              ids(
                  spamReports
                      .findForAdmin(null, "aSpamTarget", null, newestFirst, FIRST_PAGE)
                      .items()));
          assertEquals(
              List.of(older, newer),
              ids(
                  spamReports
                      .findForAdmin(
                          null,
                          "aSpamTarget",
                          null,
                          new SortSpec("username", SortDirection.ASC),
                          FIRST_PAGE)
                      .items()));
          assertEquals(
              List.of(newer),
              ids(
                  spamReports
                      .findForAdmin(null, "aspamtargetb", null, newestFirst, FIRST_PAGE)
                      .items()));

          LocalDate today = today();
          assertEquals(
              2,
              spamReports
                  .findForAdmin(
                      new DateRange(today.minusDays(1), today.plusDays(1)),
                      "aSpamTarget",
                      SpamReportStatus.PENDING,
                      newestFirst,
                      FIRST_PAGE)
                  .totalItems());

          spamReports.updateReview(newer, SpamReportStatus.RESOLVED, admin, "Da khoa tai khoan");
          SpamReport resolved = spamReports.findById(newer).orElseThrow();
          assertEquals(SpamReportStatus.RESOLVED, resolved.status());
          assertEquals(admin, resolved.reviewedBy().userId());
          assertEquals(reporter, resolved.reporter().userId());
          assertEquals(second, resolved.reportedUser().userId());
          assertNotNull(resolved.resolvedAt());

          spamReports.updateReview(newer, SpamReportStatus.REVIEWING, admin, null);
          assertNull(spamReports.findById(newer).orElseThrow().resolvedAt());
        });
  }

  @Test
  void computesActivityStatisticsForARange() {
    inRollback(
        () -> {
          long busy = newUser("aActBusy");
          long quiet = newUser("aActQuiet");
          long friend1 = newUser("aActFriendA");
          long friend2 = newUser("aActFriendB");
          long chat1 = conversations.createDirect(busy, friend1);
          long chat2 = conversations.createDirect(busy, friend2);
          long group = conversations.createGroup("Act group", busy, false);
          members.addOrReactivate(group, busy, GroupRole.ADMIN);

          activity.recordAppOpen(busy);
          activity.recordAppOpen(busy);
          activity.recordDirectChat(busy, chat1, friend1);
          activity.recordDirectChat(busy, chat1, friend1);
          activity.recordDirectChat(busy, chat2, friend2);
          activity.recordGroupChat(busy, group);
          activity.recordAppOpen(quiet);
          users.softDelete(quiet);

          LocalDate today = today();
          DateRange range = new DateRange(today.minusDays(1), today.plusDays(1));
          SortSpec byName = new SortSpec("name", SortDirection.ASC);

          List<ActivityStatisticRow> rows =
              activity.findStatistics(range, "aAct", null, null, null, byName, FIRST_PAGE).items();
          assertEquals(
              2, rows.size(), "users with events are listed, including soft-deleted quiet");
          ActivityStatisticRow busyRow = rows.get(0);
          assertEquals(busy, busyRow.user().userId());
          assertEquals(2, busyRow.appOpens());
          assertEquals(2, busyRow.chattedUsers());
          assertEquals(1, busyRow.chattedGroups());

          assertEquals(
              List.of(quiet),
              activity
                  .findStatistics(
                      range,
                      "aAct",
                      new NumericFilter(ComparisonOperator.LT, 2),
                      new NumericFilter(ComparisonOperator.EQ, 0),
                      null,
                      byName,
                      FIRST_PAGE)
                  .items()
                  .stream()
                  .map(r -> r.user().userId())
                  .toList());
          assertEquals(
              0,
              activity
                  .findStatistics(
                      new DateRange(today.minusDays(9), today.minusDays(2)),
                      "aAct",
                      null,
                      null,
                      null,
                      byName,
                      FIRST_PAGE)
                  .totalItems());
        });
  }

  @Test
  void countsDistinctMonthlyActiveUsers() {
    inRollback(
        () -> {
          int year = today().getYear();
          int month = today().getMonthValue() - 1;
          long before = activity.countMonthlyActiveUsers(year)[month];
          long user = newUser("aMau");
          activity.recordAppOpen(user);
          activity.recordAppOpen(user);

          long[] counts = activity.countMonthlyActiveUsers(year);
          assertEquals(12, counts.length);
          assertEquals(before + 1, counts[month], "a user counts once per month");
        });
  }

  private static List<Long> ids(List<SpamReport> reports) {
    return reports.stream().map(SpamReport::spamReportId).toList();
  }
}
