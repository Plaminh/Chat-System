package com.chat.server.model;

import com.chat.common.enums.AccountStatus;
import java.time.OffsetDateTime;

/** Light {@code users} projection for lists; online presence is runtime state, not stored. */
public record UserBrief(
    long userId,
    String username,
    String fullName,
    AccountStatus accountStatus,
    OffsetDateTime createdAt) {}
