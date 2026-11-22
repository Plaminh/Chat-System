package com.chat.server.model;

import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** One row of {@code users}. The password hash never leaves the server. */
public record User(
    long userId,
    String username,
    String passwordHash,
    String email,
    String fullName,
    String address,
    LocalDate dateOfBirth,
    Gender gender,
    UserRole role,
    AccountStatus accountStatus,
    OffsetDateTime lockedAt,
    OffsetDateTime deletedAt,
    OffsetDateTime passwordChangedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  @Override
  public String toString() {
    return "User[userId="
        + userId
        + ", username="
        + username
        + ", role="
        + role
        + ", accountStatus="
        + accountStatus
        + ", passwordHash=<redacted>]";
  }
}
