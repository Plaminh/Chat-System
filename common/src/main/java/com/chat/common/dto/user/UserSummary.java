package com.chat.common.dto.user;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;
import static com.chat.common.protocol.ContractChecks.text;

import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.PresenceStatus;

public record UserSummary(
    long userId,
    String username,
    String fullName,
    AccountStatus accountStatus,
    PresenceStatus presence) {
  public UserSummary {
    userId = positive(userId, "userId");
    username = text(username, "username");
    fullName = text(fullName, "fullName");
    accountStatus = required(accountStatus, "accountStatus");
    presence = required(presence, "presence");
  }
}
