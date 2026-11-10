package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JLabel;
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
    assertTrue(dialog.getFeedbackLabel().getText().contains("valid email"));
  }

  @Test
  void validEmailDescribesFutureTemporaryPasswordWorkflowWithoutClaimingDelivery() {
    PasswordRecoveryDialog dialog = new PasswordRecoveryDialog();
    dialog.getEmailField().setText("sample@example.test");

    dialog.submitRecoveryRequest();

    String feedback = dialog.getFeedbackLabel().getText().toLowerCase();
    String visibleText = collectLabelText(dialog).toLowerCase();
    assertTrue(dialog.isRequestSubmitted());
    assertTrue(visibleText.contains("randomly generated temporary password"));
    assertTrue(feedback.contains("temporary password will be generated and sent"));
    assertTrue(feedback.contains("registered email address"));
    assertFalse(feedback.contains("has been sent"));
    assertFalse(feedback.contains("was sent"));
    assertFalse(feedback.contains("temporary password:"));
  }

  private String collectLabelText(Container container) {
    StringBuilder text = new StringBuilder();
    for (Component component : container.getComponents()) {
      if (component instanceof JLabel label) {
        text.append(' ').append(label.getText());
      }
      if (component instanceof Container child) {
        text.append(collectLabelText(child));
      }
    }
    return text.toString();
  }
}
