package com.chat.server.model;

import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import java.time.LocalDate;

/** Values for inserting a {@code users} row. {@code passwordHash} must already be a BCrypt hash. */
public record NewUser(
    String username,
    String passwordHash,
    String email,
    String fullName,
    String address,
    LocalDate dateOfBirth,
    Gender gender,
    UserRole role) {

  @Override
  public String toString() {
    return "NewUser[username=" + username + ", role=" + role + ", passwordHash=<redacted>]";
  }
}
