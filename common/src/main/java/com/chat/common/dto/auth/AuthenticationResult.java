package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserSummary;
import java.time.OffsetDateTime;

public record AuthenticationResult(UserSummary user, OffsetDateTime authenticatedAt) {
  public AuthenticationResult {
    user = required(user, "user");
    authenticatedAt = required(authenticatedAt, "authenticatedAt");
  }
}
