package com.chat.user.model;

import java.util.Objects;

public record GroupMember(UserProfile user, Role role) {
  public enum Role {
    ADMIN,
    MEMBER
  }

  public GroupMember {
    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(role, "role");
  }

  public GroupMember withRole(Role nextRole) {
    return new GroupMember(user, nextRole);
  }
}
