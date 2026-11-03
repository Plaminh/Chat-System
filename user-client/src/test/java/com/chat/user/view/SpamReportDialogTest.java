package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.UserProfile;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SpamReportDialogTest {
  @Test
  void reportRequiresPriorInteractionAndAReason() {
    SpamReportDialog noPriorChat = new SpamReportDialog(profile(), false);
    noPriorChat.getReasonField().setText("Sample reason");
    noPriorChat.submitReport();
    assertFalse(noPriorChat.isReportSubmitted());

    SpamReportDialog blankReason = new SpamReportDialog(profile(), true);
    blankReason.getReasonField().setText("   ");
    blankReason.submitReport();
    assertFalse(blankReason.isReportSubmitted());
    assertTrue(blankReason.getFeedbackLabel().getText().contains("reason"));
  }

  @Test
  void eligibleReportShowsSampleOnlyConfirmation() {
    SpamReportDialog dialog = new SpamReportDialog(profile(), true);
    dialog.getReasonField().setText("Repeated unsolicited sample messages");

    dialog.submitReport();

    assertTrue(dialog.isReportSubmitted());
    assertTrue(dialog.getFeedbackLabel().getText().contains("not sent"));
  }

  private UserProfile profile() {
    return new UserProfile(
        "sample.user",
        "Sample User",
        "sample@example.test",
        "Sample address",
        LocalDate.of(2000, 1, 1),
        "Not specified");
  }
}
