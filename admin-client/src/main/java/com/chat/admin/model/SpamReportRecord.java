package com.chat.admin.model;

import java.time.LocalDateTime;

/** Represents a user-submitted spam report in the Admin Moderation panel (Requirement 1.2.4). */
public final class SpamReportRecord {
  private final String reportId;
  private final String reporterUsername;
  private final String reportedUsername;
  private final String reason;
  private final LocalDateTime reportedAt;
  private String status;

  public SpamReportRecord(
      String reportId,
      String reporterUsername,
      String reportedUsername,
      String reason,
      LocalDateTime reportedAt,
      String status) {
    this.reportId = reportId;
    this.reporterUsername = reporterUsername;
    this.reportedUsername = reportedUsername;
    this.reason = reason;
    this.reportedAt = reportedAt;
    this.status = status;
  }

  public String getReportId() {
    return reportId;
  }

  public String getReporterUsername() {
    return reporterUsername;
  }

  public String getReportedUsername() {
    return reportedUsername;
  }

  public String getReason() {
    return reason;
  }

  public LocalDateTime getReportedAt() {
    return reportedAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
