package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.text;

public record SpamReportCreateRequest(long reportedUserId, Long messageId, String reason) {
  public SpamReportCreateRequest {
    reportedUserId = positive(reportedUserId, "reportedUserId");
    reason = text(reason, "reason");
    if (messageId != null && messageId <= 0) {
      throw new IllegalArgumentException("messageId must be positive");
    }
  }
}
