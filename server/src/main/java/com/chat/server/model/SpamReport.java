package com.chat.server.model;

import com.chat.common.enums.SpamReportStatus;
import java.time.OffsetDateTime;

/**
 * One {@code spam_reports} row joined with the reporter, reported user and reviewer. Each user is
 * null when that account reference was removed.
 */
public record SpamReport(
    long spamReportId,
    UserBrief reporter,
    UserBrief reportedUser,
    Long messageId,
    String reason,
    SpamReportStatus status,
    UserBrief reviewedBy,
    String resolutionNote,
    OffsetDateTime createdAt,
    OffsetDateTime resolvedAt) {}
