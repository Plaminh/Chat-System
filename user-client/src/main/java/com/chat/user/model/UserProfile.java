package com.chat.user.model;

import java.time.LocalDate;
import java.util.Objects;

public record UserProfile(
    String username,
    String fullName,
    String email,
    String address,
    LocalDate dateOfBirth,
    String gender) {
  public UserProfile {
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(fullName, "fullName");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(dateOfBirth, "dateOfBirth");
    Objects.requireNonNull(gender, "gender");
  }
}
