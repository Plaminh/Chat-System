package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

public record PasswordChangeRequest(String currentPassword, String newPassword) {
  public PasswordChangeRequest {
    currentPassword = text(currentPassword, "currentPassword");
    newPassword = text(newPassword, "newPassword");
  }
}
