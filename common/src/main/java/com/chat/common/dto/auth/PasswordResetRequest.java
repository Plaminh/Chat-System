package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

@Deprecated(forRemoval = false)
public record PasswordResetRequest(String resetCode, String newPassword) {
  public PasswordResetRequest {
    resetCode = text(resetCode, "resetCode");
    newPassword = text(newPassword, "newPassword");
  }

  @Override
  public String toString() {
    return "PasswordResetRequest[resetCode=<redacted>, newPassword=<redacted>]";
  }
}
