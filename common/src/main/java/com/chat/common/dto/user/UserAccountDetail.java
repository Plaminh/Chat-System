package com.chat.common.dto.user;

import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.enums.Gender;
import com.chat.common.enums.UserRole;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UserAccountDetail(
    UserSummary summary,
    String email,
    String address,
    LocalDate dateOfBirth,
    Gender gender,
    UserRole role,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
  public UserAccountDetail {
    summary = required(summary, "summary");
    email = text(email, "email");
    address = text(address, "address");
    dateOfBirth = required(dateOfBirth, "dateOfBirth");
    role = required(role, "role");
    createdAt = required(createdAt, "createdAt");
    updatedAt = required(updatedAt, "updatedAt");
  }
}
