package com.chat.admin.controller;

import com.chat.admin.model.AdminMockDataStore;
import com.chat.admin.model.AdminUser;
import com.chat.admin.model.SpamReportRecord;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý kiểm duyệt báo cáo vi phạm/spam cho SpamReportPanel.
 */
public final class SpamModerationController {
  private final AdminMockDataStore dataStore;

  public SpamModerationController() {
    this(AdminMockDataStore.getInstance());
  }

  public SpamModerationController(AdminMockDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public List<SpamReportRecord> filterAndSortReports(
      String usernameFilter, LocalDate fromDate, LocalDate toDate, int sortIndex) {
    String query = usernameFilter == null ? "" : usernameFilter.trim().toLowerCase();

    List<SpamReportRecord> filtered = new ArrayList<>();
    for (SpamReportRecord r : dataStore.getSpamReports()) {
      LocalDate reportDate = r.getReportedAt().toLocalDate();
      if (fromDate != null && reportDate.isBefore(fromDate)) {
        continue;
      }
      if (toDate != null && reportDate.isAfter(toDate)) {
        continue;
      }
      if (!query.isEmpty()
          && !r.getReportedUsername().toLowerCase().contains(query)
          && !r.getReporterUsername().toLowerCase().contains(query)) {
        continue;
      }
      filtered.add(r);
    }

    Comparator<SpamReportRecord> comparator =
        switch (sortIndex) {
          case 1 -> Comparator.comparing(SpamReportRecord::getReportedAt);
          case 2 -> Comparator.comparing(r -> r.getReportedUsername().toLowerCase());
          case 3 ->
              Comparator.comparing((SpamReportRecord r) -> r.getReportedUsername().toLowerCase())
                  .reversed();
          default -> Comparator.comparing(SpamReportRecord::getReportedAt).reversed();
        };
    filtered.sort(comparator);
    return filtered;
  }

  public record SpamKpiStats(int totalReports, long pendingCount, long lockedCount) {}

  public SpamKpiStats getKpiStats() {
    List<SpamReportRecord> all = dataStore.getSpamReports();
    long pendingCount =
        all.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
    long lockedCount = all.size() - pendingCount;
    return new SpamKpiStats(all.size(), pendingCount, lockedCount);
  }

  public Optional<AdminUser> findUserByUsername(String username) {
    return dataStore.findUserByUsername(username);
  }

  public void lockUserAccount(String username) {
    dataStore.lockUserAccount(username);
  }

  public void addDataChangeListener(Runnable listener) {
    dataStore.addChangeListener(listener);
  }
}
