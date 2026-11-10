package com.chat.common.dto.admin;

import com.chat.common.dto.auth.RegisterRequest;
import com.chat.common.enums.UserRole;
import static com.chat.common.protocol.ContractChecks.required;

public record AdminCreateUserRequest(RegisterRequest account, UserRole role) {
  public AdminCreateUserRequest {
    account = required(account, "account");
    role = required(role, "role");
  }
}
