package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.RegisterRequest;
import com.chat.common.enums.ErrorCode;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class RegistrationDialogTest {
  @Test
  void requiredFieldsAreValidatedWithoutSubmitting() throws Exception {
    RegistrationDialog dialog = new RegistrationDialog();

    dialog.submitRegistration();
    SwingUtilities.invokeAndWait(() -> {});

    assertNull(dialog.getSubmittedProfile());
    assertTrue(dialog.getFeedbackLabel().getText().contains("required fields"));
  }

  @Test
  void mismatchedPasswordsAreRejected() throws Exception {
    RegistrationDialog dialog = validDialog();
    dialog.getConfirmPasswordField().setText("different-password");

    dialog.submitRegistration();
    SwingUtilities.invokeAndWait(() -> {});

    assertNull(dialog.getSubmittedProfile());
    assertTrue(dialog.getFeedbackLabel().getText().contains("do not match"));
  }

  @Test
  void validSubmissionSendsRegistrationAndClearsPasswordInputs() throws Exception {
    AtomicReference<RegisterRequest> sent = new AtomicReference<>();
    RegistrationDialog dialog =
        new RegistrationDialog(
            request -> {
              sent.set(request);
              return CompletableFuture.completedFuture(
                  new ResponseEnvelope("request-1", true, null, null, null));
            });
    fillValidDialog(dialog);

    dialog.submitRegistration();
    SwingUtilities.invokeAndWait(() -> {});

    assertEquals("sample.user", dialog.getSubmittedProfile().username());
    assertEquals(LocalDate.of(2000, 1, 1), dialog.getSubmittedProfile().dateOfBirth());
    assertEquals(0, dialog.getPasswordField().getPassword().length);
    assertEquals(0, dialog.getConfirmPasswordField().getPassword().length);
    assertEquals("sample.user", sent.get().username());
    assertEquals("StrongPassword123", sent.get().password());
    assertTrue(dialog.getFeedbackLabel().getText().contains("Account created"));
  }

  @Test
  void duplicateUsernameShowsServerError() throws Exception {
    RegistrationDialog dialog =
        new RegistrationDialog(
            request ->
                CompletableFuture.completedFuture(
                    new ResponseEnvelope(
                        "request-1",
                        false,
                        null,
                        ErrorCode.ALREADY_EXISTS,
                        "Username already exists")));
    fillValidDialog(dialog);

    dialog.submitRegistration();
    SwingUtilities.invokeAndWait(() -> {});

    assertNull(dialog.getSubmittedProfile());
    assertEquals("Username already exists", dialog.getFeedbackLabel().getText());
  }

  private RegistrationDialog validDialog() {
    RegistrationDialog dialog = new RegistrationDialog();
    fillValidDialog(dialog);
    return dialog;
  }

  private void fillValidDialog(RegistrationDialog dialog) {
    dialog.getFullNameField().setText("Sample User");
    dialog.getUsernameField().setText("sample.user");
    dialog.getEmailField().setText("sample@example.test");
    dialog.getAddressField().setText("Sample address");
    dialog.getDateOfBirthField().setText("2000-01-01");
    dialog.getGenderField().setText("Not specified");
    dialog.getPasswordField().setText("StrongPassword123");
    dialog.getConfirmPasswordField().setText("StrongPassword123");
  }
}
