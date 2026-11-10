package com.chat.common.dto.auth;

import static com.chat.common.protocol.ContractChecks.text;

public record LoginRequest(String username, String password) {
  public LoginRequest {
    username = text(username, "username");
    password = text(password, "password");
  }
}
