package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.UserProfile;
import com.chat.user.model.UserSessionState;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProfileSettingsPanelTest {
  @Test
  void cancelRestoresValuesWithoutUpdatingSession() {
    UserSessionState session = UserSessionState.createSample();
    UserProfile original = session.getProfile();
    ProfileSettingsPanel panel = new ProfileSettingsPanel(session);
    panel.getFullNameField().setText("Unsaved Name");
    panel.getEmailField().setText("changed@example.test");

    panel.cancelChanges();

    assertEquals(original, session.getProfile());
    assertEquals(original.fullName(), panel.getFullNameField().getText());
  }

  @Test
  void saveUpdatesOnlyProjectProfileFields() {
    UserSessionState session = UserSessionState.createSample();
    ProfileSettingsPanel panel = new ProfileSettingsPanel(session);
    panel.getFullNameField().setText("Updated Name");
    panel.getEmailField().setText("updated@example.test");
    panel.getAddressField().setText("Updated address");
    panel.getDateOfBirthField().setText("1999-02-03");
    panel.getGenderField().setText("Not specified");

    panel.saveProfile();

    assertEquals("Updated Name", session.getProfile().fullName());
    assertEquals("alex.morgan", session.getProfile().username());
    assertEquals(LocalDate.of(1999, 2, 3), session.getProfile().dateOfBirth());
    assertTrue(panel.getFeedbackLabel().getText().contains("trong phiên này"));
  }

  @Test
  void changePasswordSampleRequiresMatchingValuesAndClearsInputs() {
    ChangePasswordDialog dialog = new ChangePasswordDialog();
    dialog.getCurrentPasswordField().setText("current");
    dialog.getNewPasswordField().setText("next");
    dialog.getConfirmPasswordField().setText("mismatch");

    dialog.submitChange();

    assertTrue(dialog.getFeedbackLabel().getText().contains("không khớp"));
    assertFalse(dialog.isChangeSubmitted());

    dialog.getCurrentPasswordField().setText("current");
    dialog.getNewPasswordField().setText("next");
    dialog.getConfirmPasswordField().setText("next");
    dialog.submitChange();

    assertTrue(dialog.isChangeSubmitted());
    assertEquals(0, dialog.getNewPasswordField().getPassword().length);
  }
}
