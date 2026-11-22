package com.chat.server.model;

import java.time.OffsetDateTime;

/** One {@code login_history} row joined with the user's username and full name. */
public record LoginHistoryRecord(
    long loginHistoryId,
    long userId,
    String username,
    String fullName,
    OffsetDateTime loggedInAt,
    OffsetDateTime loggedOutAt) {}
