package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.SpamReportStatus;

public record SpamReportUpdate(
    long spamReportId, SpamReportStatus status, String resolutionNote) {
  public SpamReportUpdate {
    spamReportId = positive(spamReportId, "spamReportId");
    status = required(status, "status");
  }
}
