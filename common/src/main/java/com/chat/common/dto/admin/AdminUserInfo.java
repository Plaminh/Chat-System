package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.dto.user.UserAccountDetail;

public record AdminUserInfo(UserAccountDetail account) {
  public AdminUserInfo {
    account = required(account, "account");
  }
}
