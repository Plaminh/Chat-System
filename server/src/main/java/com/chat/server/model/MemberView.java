package com.chat.server.model;

import com.chat.common.enums.GroupRole;
import java.time.OffsetDateTime;

/** An active conversation member joined with the user. */
public record MemberView(UserBrief user, GroupRole role, OffsetDateTime joinedAt) {}
