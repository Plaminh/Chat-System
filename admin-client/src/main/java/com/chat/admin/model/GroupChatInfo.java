package com.chat.admin.model;

import java.time.LocalDate;
import java.util.List;

/** Represents a group chat and its member/admin roster (Requirement 1.2.3). */
public record GroupChatInfo(
    String groupId, String groupName, LocalDate createdAt, List<GroupMemberInfo> members) {

  /** Represents an individual member in a group chat and whether they hold Group Admin role. */
  public record GroupMemberInfo(String username, String fullName, boolean groupAdmin) {}

  public int getMemberCount() {
    return members == null ? 0 : members.size();
  }

  public long getAdminCount() {
    return members == null ? 0 : members.stream().filter(GroupMemberInfo::groupAdmin).count();
  }
}
