package com.chat.user.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ChatHistoryActions {
  public enum Action {
    DELETE_FOR_ME,
    UNSEND,
    CLEAR_HISTORY
  }

  public void applyMessageAction(
      Action action, Conversation conversation, Set<String> messageIds) {
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(conversation, "conversation");
    Objects.requireNonNull(messageIds, "messageIds");
    if (action == Action.CLEAR_HISTORY) {
      throw new IllegalArgumentException("Clear history does not target individual messages");
    }
    if (messageIds.isEmpty()) {
      throw new IllegalArgumentException("Select at least one message");
    }

    Set<String> existingIds =
        conversation.messages().stream()
            .map(ChatMessage::messageId)
            .collect(java.util.stream.Collectors.toSet());
    if (!existingIds.containsAll(messageIds)) {
      throw new IllegalArgumentException("Selected messages are not in the conversation");
    }

    Set<String> remaining = new HashSet<>(messageIds);
    for (ChatMessage message : conversation.messages()) {
      if (!remaining.remove(message.messageId())) {
        continue;
      }
      ChatMessage updated =
          action == Action.DELETE_FOR_ME
              ? message.removeForCurrentUser()
              : message.markUnsent();
      conversation.replaceMessage(message.messageId(), updated);
    }
  }

  public void clearHistory(Conversation conversation) {
    Objects.requireNonNull(conversation, "conversation").clearMessages();
  }
}
