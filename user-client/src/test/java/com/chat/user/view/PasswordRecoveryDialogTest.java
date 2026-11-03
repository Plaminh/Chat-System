package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordRecoveryDialogTest {
  @Test
  void blankAndInvalidEmailAreRejected() {
    PasswordRecoveryDialog dialog = new PasswordRecoveryDialog();

    dialog.submitRecoveryRequest();
    assertFalse(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("email"));

    dialog.getEmailField().setText("not-an-email");
    dialog.submitRecoveryRequest();
    assertFalse(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("chưa hợp lệ"));
  }

  @Test
  void validEmailShowsNeutralConfirmationWithoutSendingOrRevealingPassword() {
    PasswordRecoveryDialog dialog = new PasswordRecoveryDialog();
    dialog.getEmailField().setText("sample@example.test");

    dialog.submitRecoveryRequest();

    assertTrue(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("không gửi email"));
    assertFalse(dialog.getFeedbackLabel().getText().contains("mật khẩu mới"));
  }
}
