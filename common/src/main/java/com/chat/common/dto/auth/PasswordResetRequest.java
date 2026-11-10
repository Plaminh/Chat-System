package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

public record PasswordResetRequest(String resetCode, String newPassword) {
  public PasswordResetRequest {
    resetCode = text(resetCode, "resetCode");
    newPassword = text(newPassword, "newPassword");
  }
}
