package com.chat.user.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record ChatMessage(
    String messageId,
    String senderUsername,
    String content,
    LocalDateTime sentAt,
    boolean removedForCurrentUser,
    boolean unsent) {
  public ChatMessage {
    Objects.requireNonNull(messageId, "messageId");
    Objects.requireNonNull(senderUsername, "senderUsername");
    Objects.requireNonNull(content, "content");
    Objects.requireNonNull(sentAt, "sentAt");
  }

  public ChatMessage removeForCurrentUser() {
    return new ChatMessage(
        messageId, senderUsername, content, sentAt, true, unsent);
  }

  public ChatMessage markUnsent() {
    return new ChatMessage(
        messageId, senderUsername, content, sentAt, removedForCurrentUser, true);
  }

  public String displayContent() {
    if (removedForCurrentUser) {
      return "";
    }
    return unsent ? "Tin nhắn đã được thu hồi trong bản mẫu." : content;
  }
}
