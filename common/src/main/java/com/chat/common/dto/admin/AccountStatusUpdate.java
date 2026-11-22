package com.chat.common.dto.admin;

import static com.chat.common.protocol.ContractChecks.positive;
import static com.chat.common.protocol.ContractChecks.required;

import com.chat.common.enums.AccountStatus;

public record AccountStatusUpdate(long userId, AccountStatus status) {
  public AccountStatusUpdate {
    userId = positive(userId, "userId");
    status = required(status, "status");
    if (status == AccountStatus.DELETED) {
      throw new IllegalArgumentException("use ADMIN_DELETE_USER for terminal deletion");
    }
  }
}
