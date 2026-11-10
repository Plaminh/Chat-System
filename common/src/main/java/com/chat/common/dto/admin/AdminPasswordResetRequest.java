package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.text;

public record AdminPasswordResetRequest(long userId, String newPassword) {
  public AdminPasswordResetRequest {
    userId = positive(userId, "userId");
    newPassword = text(newPassword, "newPassword");
  }
}
