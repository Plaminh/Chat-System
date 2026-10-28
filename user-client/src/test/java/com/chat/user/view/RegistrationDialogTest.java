package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RegistrationDialogTest {
  @Test
  void requiredFieldsAreValidatedWithoutSubmitting() {
    RegistrationDialog dialog = new RegistrationDialog();

    dialog.submitRegistration();

    assertNull(dialog.getSubmittedProfile());
    assertTrue(dialog.getFeedbackLabel().getText().contains("đầy đủ"));
  }

  @Test
  void mismatchedPasswordsAreRejected() {
    RegistrationDialog dialog = validDialog();
    dialog.getConfirmPasswordField().setText("different-password");

    dialog.submitRegistration();

    assertNull(dialog.getSubmittedProfile());
    assertTrue(dialog.getFeedbackLabel().getText().contains("không khớp"));
  }

  @Test
  void validSubmissionCreatesOnlySampleProfileAndClearsPasswordInputs() {
    RegistrationDialog dialog = validDialog();

    dialog.submitRegistration();

    assertEquals("sample.user", dialog.getSubmittedProfile().username());
    assertEquals(LocalDate.of(2000, 1, 1), dialog.getSubmittedProfile().dateOfBirth());
    assertEquals(0, dialog.getPasswordField().getPassword().length);
    assertEquals(0, dialog.getConfirmPasswordField().getPassword().length);
    assertTrue(dialog.getFeedbackLabel().getText().contains("chưa được tạo"));
  }

  private RegistrationDialog validDialog() {
    RegistrationDialog dialog = new RegistrationDialog();
    dialog.getFullNameField().setText("Sample User");
    dialog.getUsernameField().setText("sample.user");
    dialog.getEmailField().setText("sample@example.test");
    dialog.getAddressField().setText("Sample address");
    dialog.getDateOfBirthField().setText("2000-01-01");
    dialog.getGenderField().setText("Not specified");
    dialog.getPasswordField().setText("sample-password");
    dialog.getConfirmPasswordField().setText("sample-password");
    return dialog;
  }
}
