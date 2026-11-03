package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class WritingAssistDialogTest {
  @Test
  void previewsSuggestionAndRevisionWithoutChangingOriginalDraft() {
    ArrayList<String> accepted = new ArrayList<>();
    WritingAssistDialog dialog = new WritingAssistDialog("Original draft", accepted::add);

    String suggestion = dialog.getPreview().getText();
    dialog.getModeSelector().setSelectedItem(WritingAssistDialog.Mode.REVISE);
    String revision = dialog.getPreview().getText();

    assertFalse(suggestion.equals(revision));
    assertTrue(dialog.getPreview().getText().contains("Original draft"));
    assertTrue(accepted.isEmpty());
  }

  @Test
  void acceptingInsertsPreviewOnlyWhenExplicitlyChosen() {
    ArrayList<String> accepted = new ArrayList<>();
    WritingAssistDialog dialog = new WritingAssistDialog("Draft to improve", accepted::add);

    dialog.acceptPreview();

    assertTrue(dialog.isAccepted());
    assertEquals(1, accepted.size());
    assertEquals(dialog.getPreview().getText(), accepted.get(0));
  }

  @Test
  void discardDoesNotInvokeDraftUpdateHandler() {
    ArrayList<String> accepted = new ArrayList<>();
    WritingAssistDialog dialog = new WritingAssistDialog("Draft to keep", accepted::add);
    javax.swing.JButton discard = findButton(dialog, "Discard");
    discard.doClick();

    assertFalse(dialog.isAccepted());
    assertTrue(accepted.isEmpty());
  }

  private javax.swing.JButton findButton(java.awt.Container container, String text) {
    javax.swing.JButton button = findButtonRecursively(container, text);
    assertNotNull(button);
    return button;
  }

  private javax.swing.JButton findButtonRecursively(java.awt.Container container, String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JButton button && button.getText().equals(text)) {
        return button;
      }
      if (component instanceof java.awt.Container nested) {
        javax.swing.JButton found = findButtonRecursively(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
