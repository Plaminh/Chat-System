package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordChangeRequest;
import com.chat.common.enums.ErrorCode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ChangePasswordDialogTest {
  @Test
  void rejectsMismatchedAndShortPasswordsWithoutSendingRequests() {
    AtomicReference<PasswordChangeRequest> sentRequest = new AtomicReference<>();
    ChangePasswordDialog dialog =
        new ChangePasswordDialog(
            request -> {
              sentRequest.set(request);
              return CompletableFuture.completedFuture(success());
            });
    dialog.getCurrentPasswordField().setText("current-password");
    dialog.getNewPasswordField().setText("new-password-123");
    dialog.getConfirmPasswordField().setText("different-password");

    dialog.submitChange();

    assertFalse(dialog.isChangeSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("do not match"));
    assertNull(sentRequest.get());

    dialog.getCurrentPasswordField().setText("current-password");
    dialog.getNewPasswordField().setText("short");
    dialog.getConfirmPasswordField().setText("short");
    dialog.submitChange();

    assertFalse(dialog.isChangeSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("at least 8"));
    assertNull(sentRequest.get());
  }

  @Test
  void sendsOnlyCurrentAndNewPasswordsAndShowsServerSuccess() throws Exception {
    AtomicReference<PasswordChangeRequest> sentRequest = new AtomicReference<>();
    ChangePasswordDialog dialog =
        new ChangePasswordDialog(
            request -> {
              sentRequest.set(request);
              return CompletableFuture.completedFuture(success());
            });
    dialog.getCurrentPasswordField().setText("current-password");
    dialog.getNewPasswordField().setText("new-password-123");
    dialog.getConfirmPasswordField().setText("new-password-123");

    dialog.submitChange();
    SwingUtilities.invokeAndWait(() -> {});

    assertTrue(dialog.isChangeSubmitted());
    assertEquals("current-password", sentRequest.get().currentPassword());
    assertEquals("new-password-123", sentRequest.get().newPassword());
    assertEquals(0, dialog.getCurrentPasswordField().getPassword().length);
    assertEquals(0, dialog.getNewPasswordField().getPassword().length);
    assertEquals(0, dialog.getConfirmPasswordField().getPassword().length);
    assertEquals("Password changed successfully.", dialog.getFeedbackLabel().getText());
  }

  @Test
  void showsTheInvalidCurrentPasswordResponseWithoutClaimingSuccess() throws Exception {
    ChangePasswordDialog dialog =
        new ChangePasswordDialog(
            request ->
                CompletableFuture.completedFuture(
                    new ResponseEnvelope(
                        "change-password",
                        false,
                        null,
                        ErrorCode.INVALID_CREDENTIALS,
                        "Current password is incorrect.")));
    enterValidFields(dialog);

    dialog.submitChange();
    SwingUtilities.invokeAndWait(() -> {});

    assertFalse(dialog.isChangeSubmitted());
    assertEquals("Current password is incorrect.", dialog.getFeedbackLabel().getText());
  }

  @Test
  void reportsNetworkFailureWithoutClaimingSuccess() throws Exception {
    ChangePasswordDialog dialog =
        new ChangePasswordDialog(
            request -> CompletableFuture.failedFuture(new IllegalStateException("offline")));
    enterValidFields(dialog);

    dialog.submitChange();
    SwingUtilities.invokeAndWait(() -> {});

    assertFalse(dialog.isChangeSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("Could not change password"));
  }

  private void enterValidFields(ChangePasswordDialog dialog) {
    dialog.getCurrentPasswordField().setText("current-password");
    dialog.getNewPasswordField().setText("new-password-123");
    dialog.getConfirmPasswordField().setText("new-password-123");
  }

  private ResponseEnvelope success() {
    return new ResponseEnvelope(
        "change-password", true, JsonNodeFactory.instance.objectNode(), null, null);
  }
}
