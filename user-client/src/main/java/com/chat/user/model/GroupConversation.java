package com.chat.user.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class GroupConversation {
  private final String groupId;
  private String name;
  private final List<GroupMember> members = new ArrayList<>();
  private final List<ChatMessage> messages = new ArrayList<>();

  public GroupConversation(
      String groupId,
      String name,
      List<GroupMember> members,
      List<ChatMessage> messages) {
    this.groupId = Objects.requireNonNull(groupId, "groupId");
    rename(name);
    this.members.addAll(Objects.requireNonNull(members, "members"));
    this.messages.addAll(Objects.requireNonNull(messages, "messages"));
    if (this.members.size() < 2) {
      throw new IllegalArgumentException("A group needs at least one participant besides its creator");
    }
    Set<String> usernames = new HashSet<>();
    for (GroupMember member : this.members) {
      if (!usernames.add(member.user().username().toLowerCase(java.util.Locale.ROOT))) {
        throw new IllegalArgumentException("Group members must have unique usernames");
      }
    }
  }

  public String groupId() {
    return groupId;
  }

  public String name() {
    return name;
  }

  public List<GroupMember> members() {
    return List.copyOf(members);
  }

  public List<ChatMessage> messages() {
    return List.copyOf(messages);
  }

  public void rename(String name) {
    Objects.requireNonNull(name, "name");
    String normalized = name.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Group name is required");
    }
    this.name = normalized;
  }

  public void addMember(UserProfile user) {
    Objects.requireNonNull(user, "user");
    if (findMember(user.username()) != null) {
      throw new IllegalArgumentException("User is already a group member");
    }
    members.add(new GroupMember(user, GroupMember.Role.MEMBER));
  }

  public boolean removeMember(String username) {
    return members.removeIf(member -> member.user().username().equalsIgnoreCase(username));
  }

  public boolean containsMember(String username) {
    return findMember(username) != null;
  }

  public void setRole(String username, GroupMember.Role role) {
    Objects.requireNonNull(role, "role");
    for (int index = 0; index < members.size(); index++) {
      GroupMember member = members.get(index);
      if (member.user().username().equalsIgnoreCase(username)) {
        members.set(index, member.withRole(role));
        return;
      }
    }
    throw new IllegalArgumentException("No group member found with username " + username);
  }

  public void addMessage(ChatMessage message) {
    messages.add(Objects.requireNonNull(message, "message"));
  }

  private GroupMember findMember(String username) {
    return members.stream()
        .filter(member -> member.user().username().equalsIgnoreCase(username))
        .findFirst()
        .orElse(null);
  }
}
