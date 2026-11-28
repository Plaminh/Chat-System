package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.user.UpdateProfileRequest;
import com.chat.common.dto.user.UserAccountDetail;
import com.chat.common.dto.user.UserSummary;
import com.chat.common.enums.AccountStatus;
import com.chat.common.enums.ActionType;
import com.chat.common.enums.ErrorCode;
import com.chat.common.enums.Gender;
import com.chat.common.enums.PresenceStatus;
import com.chat.user.model.UserProfile;
import com.chat.user.model.UserSessionState;
import com.chat.user.network.SocketClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ProfileSettingsPanelTest {
  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Test
  void loadsCurrentProfileFromServerAndDiscardsUnsavedChanges() throws Exception {
    UserSessionState session = UserSessionState.createSample();
    UserProfile serverProfile =
        new UserProfile(
            "member",
            "Current Name",
            "current@example.test",
            "Current address",
            LocalDate.of(1998, 4, 5),
            "FEMALE");
    ProfileSettingsPanel panel = newPanel(session, () -> success(serverProfile));

    panel.refreshProfile();
    flushEdt();

    assertEquals("Current Name", panel.getFullNameField().getText());
    assertEquals(serverProfile, session.getProfile());
    panel.getFullNameField().setText("Unsaved Name");
    panel.getEmailField().setText("changed@example.test");

    panel.cancelChanges();

    assertEquals(serverProfile, session.getProfile());
    assertEquals("Current Name", panel.getFullNameField().getText());
  }

  @Test
  void sendsUpdateToServerAndUsesTheReturnedProfile() throws Exception {
    UserSessionState session = UserSessionState.createSample();
    AtomicReference<UpdateProfileRequest> sentPayload = new AtomicReference<>();
    UserProfile savedProfile =
        new UserProfile(
            "member",
            "Updated Name",
            "updated@example.test",
            "Updated address",
            LocalDate.of(1999, 2, 3),
            "OTHER");
    ProfileSettingsPanel panel =
        newPanel(
            session,
            () ->
                success(
                    new UserProfile(
                        "member",
                        "Current Name",
                        "current@example.test",
                        "Current address",
                        LocalDate.of(1998, 4, 5),
                        "FEMALE")),
            payload -> {
              sentPayload.set(payload);
              return success(savedProfile);
            });

    panel.refreshProfile();
    flushEdt();
    panel.getFullNameField().setText("Updated Name");
    panel.getEmailField().setText("updated@example.test");
    panel.getAddressField().setText("Updated address");
    panel.getDateOfBirthField().setText("1999-02-03");
    panel.getGenderField().setText("OTHER");

    panel.saveProfile();
    flushEdt();

    assertEquals("Updated Name", session.getProfile().fullName());
    assertEquals("member", session.getProfile().username());
    assertEquals(LocalDate.of(1999, 2, 3), session.getProfile().dateOfBirth());
    assertEquals("updated@example.test", session.getProfile().email());
    assertEquals(
        new UpdateProfileRequest(
            "Updated Name",
            "updated@example.test",
            "Updated address",
            LocalDate.of(1999, 2, 3),
            Gender.OTHER),
        sentPayload.get());
    assertEquals("Profile updated successfully.", panel.getFeedbackLabel().getText());
  }

  @Test
  void failedDuplicateEmailUpdateDoesNotChangeTheSessionProfile() throws Exception {
    UserSessionState session = UserSessionState.createSample();
    UserProfile current =
        new UserProfile(
            "member",
            "Current Name",
            "current@example.test",
            "Current address",
            LocalDate.of(1998, 4, 5),
            "FEMALE");
    ProfileSettingsPanel panel =
        newPanel(
            session,
            () -> success(current),
            payload ->
                CompletableFuture.completedFuture(
                    new ResponseEnvelope(
                        "profile-request",
                        false,
                        null,
                        ErrorCode.ALREADY_EXISTS,
                        "Email already exists")));

    panel.refreshProfile();
    flushEdt();
    panel.getFullNameField().setText("Unsaved Name");
    panel.getEmailField().setText("taken@example.test");

    panel.saveProfile();
    flushEdt();

    assertEquals(current, session.getProfile());
    assertEquals("Email already exists", panel.getFeedbackLabel().getText());
    assertEquals("Unsaved Name", panel.getFullNameField().getText());
  }

  private ProfileSettingsPanel newPanel(UserSessionState session, ProfileResponse getProfile) {
    return newPanel(session, getProfile, payload -> success(session.getProfile()));
  }

  private ProfileSettingsPanel newPanel(
      UserSessionState session, ProfileResponse getProfile, ProfileUpdate updateProfile) {
    SocketClient decoder = new SocketClient();
    return new ProfileSettingsPanel(
        session,
        decoder,
        (action, payload) -> {
          if (action == ActionType.GET_PROFILE) {
            return getProfile.response();
          }
          assertEquals(ActionType.UPDATE_PROFILE, action);
          return updateProfile.response((UpdateProfileRequest) payload);
        });
  }

  private CompletableFuture<ResponseEnvelope> success(UserProfile profile) {
    return successResponse(accountDetail(profile));
  }

  private CompletableFuture<ResponseEnvelope> successResponse(UserAccountDetail detail) {
    return CompletableFuture.completedFuture(
        new ResponseEnvelope("profile-request", true, mapper.valueToTree(detail), null, null));
  }

  private UserAccountDetail accountDetail(UserProfile profile) {
    return new UserAccountDetail(
        new UserSummary(
            42L,
            profile.username(),
            profile.fullName(),
            AccountStatus.ACTIVE,
            PresenceStatus.OFFLINE),
        profile.email(),
        profile.address(),
        profile.dateOfBirth(),
        profile.gender().equals("Not specified") ? null : Gender.valueOf(profile.gender()),
        com.chat.common.enums.UserRole.USER,
        OffsetDateTime.parse("2026-01-02T03:04:05Z"),
        OffsetDateTime.parse("2026-01-02T03:04:05Z"));
  }

  private void flushEdt() throws Exception {
    SwingUtilities.invokeAndWait(() -> {});
  }

  @FunctionalInterface
  private interface ProfileResponse {
    CompletableFuture<ResponseEnvelope> response();
  }

  @FunctionalInterface
  private interface ProfileUpdate {
    CompletableFuture<ResponseEnvelope> response(UpdateProfileRequest payload);
  }
}
