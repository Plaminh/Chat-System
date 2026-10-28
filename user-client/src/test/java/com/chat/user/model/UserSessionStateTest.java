package com.chat.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class UserSessionStateTest {
  @Test
  void sampleSessionStartsSignedOutWithProfileData() {
    UserSessionState state = UserSessionState.createSample();

    assertFalse(state.isSignedIn());
    assertEquals("alex.morgan", state.getProfile().username());
    assertEquals("alex@example.test", state.getProfile().email());
  }

  @Test
  void signInAndSignOutChangeOnlySessionStatus() {
    UserSessionState state = UserSessionState.createSample();
    UserProfile profile = state.getProfile();

    state.signIn();
    assertTrue(state.isSignedIn());

    state.signOut();
    assertFalse(state.isSignedIn());
    assertEquals(profile, state.getProfile());
  }

  @Test
  void profileCanBeUpdatedWithoutStoringCredentials() {
    UserSessionState state = UserSessionState.createSample();
    UserProfile updated =
        new UserProfile(
            "alex.morgan",
            "Alex Morgan",
            "alex.morgan@example.test",
            "Updated sample address",
            LocalDate.of(2000, 1, 1),
            "Not specified");

    state.updateProfile(updated);

    assertEquals(updated, state.getProfile());
    assertThrows(NullPointerException.class, () -> state.updateProfile(null));
  }
}
