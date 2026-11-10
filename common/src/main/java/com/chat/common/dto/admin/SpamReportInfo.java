package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.SpamReportStatus;
import java.time.OffsetDateTime;

public record SpamReportInfo(
    long spamReportId,
    UserSummary reporter,
    UserSummary reportedUser,
    Long messageId,
    String reason,
    SpamReportStatus status,
    UserSummary reviewedBy,
    String resolutionNote,
    OffsetDateTime createdAt,
    OffsetDateTime resolvedAt) {
  public SpamReportInfo {
    spamReportId = positive(spamReportId, "spamReportId");
    reason = text(reason, "reason");
    status = required(status, "status");
    createdAt = required(createdAt, "createdAt");
    if (messageId != null && messageId <= 0) {
      throw new IllegalArgumentException("messageId must be positive");
    }
  }
}
