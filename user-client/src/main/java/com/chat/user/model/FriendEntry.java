package com.chat.user.model;

import java.util.Objects;

public record FriendEntry(UserProfile profile, boolean online) {
  public FriendEntry {
    Objects.requireNonNull(profile, "profile");
  }

  public String username() {
    return profile.username();
  }

  public String fullName() {
    return profile.fullName();
  }
}
