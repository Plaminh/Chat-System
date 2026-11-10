package com.chat.common.dto.user;

import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.enums.Gender;
import java.time.LocalDate;

public record UpdateProfileRequest(
    String fullName, String email, String address, LocalDate dateOfBirth, Gender gender) {
  public UpdateProfileRequest {
    fullName = text(fullName, "fullName");
    email = text(email, "email");
    address = text(address, "address");
    dateOfBirth = required(dateOfBirth, "dateOfBirth");
  }
}
