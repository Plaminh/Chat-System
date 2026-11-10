package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.enums.Gender;
import java.time.LocalDate;

public record RegisterRequest(
    String username,
    String password,
    String email,
    String fullName,
    String address,
    LocalDate dateOfBirth,
    Gender gender) {
  public RegisterRequest {
    username = text(username, "username");
    password = text(password, "password");
    email = text(email, "email");
    fullName = text(fullName, "fullName");
    address = text(address, "address");
    if (dateOfBirth == null) {
      throw new IllegalArgumentException("dateOfBirth is required");
    }
  }
}
