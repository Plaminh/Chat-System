package com.chat.user.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Conversation {
  private final String conversationId;
  private final String username;
  private final String displayName;
  private final List<ChatMessage> messages = new ArrayList<>();

  public Conversation(
      String conversationId, String username, String displayName, List<ChatMessage> messages) {
    this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
    this.username = Objects.requireNonNull(username, "username");
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.messages.addAll(Objects.requireNonNull(messages, "messages"));
  }

  public String conversationId() {
    return conversationId;
  }

  public String username() {
    return username;
  }

  public String displayName() {
    return displayName;
  }

  public List<ChatMessage> messages() {
    return List.copyOf(messages);
  }

  public void addMessage(ChatMessage message) {
    messages.add(Objects.requireNonNull(message, "message"));
  }

  public void replaceMessage(String messageId, ChatMessage replacement) {
    Objects.requireNonNull(messageId, "messageId");
    Objects.requireNonNull(replacement, "replacement");
    for (int index = 0; index < messages.size(); index++) {
      if (messages.get(index).messageId().equals(messageId)) {
        messages.set(index, replacement);
        return;
      }
    }
    throw new IllegalArgumentException("No message found with ID " + messageId);
  }

  public void clearMessages() {
    messages.clear();
  }
}
