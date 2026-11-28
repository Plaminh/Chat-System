package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.common.dto.ResponseEnvelope;
import com.chat.common.dto.auth.PasswordRecoveryRequest;
import com.chat.common.enums.ErrorCode;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class PasswordRecoveryDialogTest {
  @Test
  void blankAndInvalidEmailAreRejectedWithoutSendingARequest() {
    AtomicReference<PasswordRecoveryRequest> sentRequest = new AtomicReference<>();
    PasswordRecoveryDialog dialog =
        new PasswordRecoveryDialog(
            request -> {
              sentRequest.set(request);
              return CompletableFuture.completedFuture(success());
            });

    dialog.submitRecoveryRequest();
    assertFalse(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("email"));

    dialog.getEmailField().setText("not-an-email");
    dialog.submitRecoveryRequest();
    assertFalse(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("valid email"));
    assertNull(sentRequest.get());
  }

  @Test
  void sendsTheEmailAndShowsANonEnumeratingSuccessMessage() throws Exception {
    AtomicReference<PasswordRecoveryRequest> sentRequest = new AtomicReference<>();
    PasswordRecoveryDialog dialog =
        new PasswordRecoveryDialog(
            request -> {
              sentRequest.set(request);
              return CompletableFuture.completedFuture(success());
            });
    dialog.getEmailField().setText("  sample@example.test  ");

    dialog.submitRecoveryRequest();
    SwingUtilities.invokeAndWait(() -> {});

    String feedback = dialog.getFeedbackLabel().getText().toLowerCase();
    String visibleText = collectLabelText(dialog).toLowerCase();
    assertTrue(dialog.isRequestSubmitted());
    assertTrue(sentRequest.get() != null);
    assertTrue(sentRequest.get().email().equals("sample@example.test"));
    assertTrue(visibleText.contains("randomly generated temporary password"));
    assertTrue(feedback.contains("if the address is registered"));
    assertFalse(feedback.contains("sample@example.test"));
    assertFalse(feedback.contains("temporary password:"));
  }

  @Test
  void serverFailureDoesNotClaimThatAResetWasCompleted() throws Exception {
    PasswordRecoveryDialog dialog =
        new PasswordRecoveryDialog(
            request ->
                CompletableFuture.completedFuture(
                    new ResponseEnvelope(
                        "request-1",
                        false,
                        null,
                        ErrorCode.INTERNAL_ERROR,
                        "An internal server error occurred")));
    dialog.getEmailField().setText("sample@example.test");

    dialog.submitRecoveryRequest();
    SwingUtilities.invokeAndWait(() -> {});

    assertFalse(dialog.isRequestSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("Could not process"));
    assertFalse(dialog.getFeedbackLabel().getText().contains("sample@example.test"));
  }

  private ResponseEnvelope success() {
    return new ResponseEnvelope(
        "request-1",
        true,
        com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode(),
        null,
        null);
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
