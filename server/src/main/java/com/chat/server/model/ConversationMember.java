package com.chat.server.model;

import com.chat.common.enums.GroupRole;
import java.time.OffsetDateTime;

/** One row of {@code conversation_members}; active while {@code leftAt} is null. */
public record ConversationMember(
    long conversationMemberId,
    long conversationId,
    long userId,
    GroupRole role,
    OffsetDateTime joinedAt,
    OffsetDateTime leftAt,
    OffsetDateTime historyClearedAt) {

  public boolean active() {
    return leftAt == null;
  }
}
