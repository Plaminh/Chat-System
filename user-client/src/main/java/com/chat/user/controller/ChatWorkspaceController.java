package com.chat.user.controller;

import com.chat.user.model.ChatMessage;
import com.chat.user.model.Conversation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Controller xử lý điều phối hội thoại chat, lọc tìm kiếm hội thoại
 * và gửi tin nhắn trong ChatWorkspacePanel.
 */
public final class ChatWorkspaceController {

  public List<Conversation> filterConversations(List<Conversation> conversations, String query) {
    if (conversations == null) {
      return List.of();
    }
    String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    if (filter.isEmpty()) {
      return List.copyOf(conversations);
    }
    return conversations.stream()
        .filter(
            c ->
                c.displayName().toLowerCase(Locale.ROOT).contains(filter)
                    || c.username().toLowerCase(Locale.ROOT).contains(filter))
        .toList();
  }

  public Conversation findOrCreateConversation(
      List<Conversation> conversations, String username, String displayName) {
    Objects.requireNonNull(conversations, "conversations");
    Objects.requireNonNull(username, "username");
    return conversations.stream()
        .filter(candidate -> candidate.username().equalsIgnoreCase(username))
        .findFirst()
        .orElseGet(
            () -> {
              Conversation created =
                  new Conversation(
                      "direct-" + UUID.randomUUID(), username, displayName, List.of());
              conversations.add(created);
              return created;
            });
  }
}
