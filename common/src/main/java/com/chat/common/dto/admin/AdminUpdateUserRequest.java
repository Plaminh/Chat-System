package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.enums.UserRole;

public record AdminUpdateUserRequest(
    long userId, UpdateProfileRequest profile, UserRole role) {
  public AdminUpdateUserRequest {
    userId = positive(userId, "userId");
    profile = required(profile, "profile");
    role = required(role, "role");
  }
}
