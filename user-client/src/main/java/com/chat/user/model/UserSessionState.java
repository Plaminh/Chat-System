package com.chat.user.model;

import java.time.LocalDate;
import java.util.Objects;

public final class UserSessionState {
  private UserProfile profile;
  private boolean signedIn;

  public UserSessionState(UserProfile profile) {
    this.profile = Objects.requireNonNull(profile, "profile");
  }

  public static UserSessionState createSample() {
    return new UserSessionState(
        new UserProfile(
            "alex.morgan",
            "Alex Morgan",
            "alex@example.test",
            "Sample address",
            LocalDate.of(2000, 1, 1),
            "Not specified"));
  }

  public UserProfile getProfile() {
    return profile;
  }

  public boolean isSignedIn() {
    return signedIn;
  }

  public void signIn() {
    signedIn = true;
  }

  public void signOut() {
    signedIn = false;
  }

  public void updateProfile(UserProfile profile) {
    this.profile = Objects.requireNonNull(profile, "profile");
  }
}
