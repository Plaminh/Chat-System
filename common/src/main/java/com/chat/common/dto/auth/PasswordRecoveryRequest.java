package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

public record PasswordRecoveryRequest(String email) {
  public PasswordRecoveryRequest {
    email = text(email, "email");
  }
}
