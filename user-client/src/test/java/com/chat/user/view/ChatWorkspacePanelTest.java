package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.Conversation;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ChatWorkspacePanelTest {
  @Test
  void displaysSelectedConversationAndSampleMessages() {
    ChatWorkspacePanel panel =
        new ChatWorkspacePanel(ChatWorkspacePanel.createSampleConversations(), "alex.morgan");

    assertEquals("Lê Mai Anh", panel.getSelectedConversation().displayName());
    assertEquals(2, panel.getSelectedConversation().messages().size());
    assertEquals(2, panel.getMessageList().getComponentCount() - 1);
  }

  @Test
  void sampleSendAddsMessageToSelectedConversationAndClearsComposer() {
    ChatWorkspacePanel panel =
        new ChatWorkspacePanel(ChatWorkspacePanel.createSampleConversations(), "alex.morgan");
    Conversation selected = panel.getSelectedConversation();
    panel.getComposer().setText("A sample reply");

    panel.sendSampleMessage();

    assertEquals(3, selected.messages().size());
    assertEquals("A sample reply", selected.messages().get(2).content());
    assertEquals("", panel.getComposer().getText());
    assertTrue(panel.getFeedbackLabel().getText().contains("not sent"));
  }

  @Test
  void emptyComposerDoesNotChangeConversation() {
    List<Conversation> conversations = ChatWorkspacePanel.createSampleConversations();
    ChatWorkspacePanel panel = new ChatWorkspacePanel(conversations, "alex.morgan");
    int initialCount = panel.getSelectedConversation().messages().size();

    panel.sendSampleMessage();

    assertEquals(initialCount, panel.getSelectedConversation().messages().size());
    assertTrue(panel.getFeedbackLabel().getText().contains("Enter a message"));
  }

  @Test
  void contextualActionsUseConfiguredHandlers() {
    ChatWorkspacePanel panel =
        new ChatWorkspacePanel(ChatWorkspacePanel.createSampleConversations(), "alex.morgan");
    AtomicInteger historyCalls = new AtomicInteger();
    List<Conversation> reported = new ArrayList<>();
    List<String> assistedDrafts = new ArrayList<>();
    panel.setActionHandlers(historyCalls::incrementAndGet, reported::add, assistedDrafts::add);
    panel.getComposer().setText("draft to assist");

    findButton(panel, "Search History").doClick();
    findButton(panel, "Report").doClick();
    findButton(panel, "Writing Assistant").doClick();

    assertEquals(1, historyCalls.get());
    assertEquals(panel.getSelectedConversation(), reported.get(0));
    assertEquals("draft to assist", assistedDrafts.get(0));
  }

  @Test
  void openingUnknownFriendCreatesLocalConversation() {
    ChatWorkspacePanel panel =
        new ChatWorkspacePanel(ChatWorkspacePanel.createSampleConversations(), "alex.morgan");

    panel.openConversationFor("new.friend", "New Friend");

    assertEquals("new.friend", panel.getSelectedConversation().username());
    assertTrue(panel.getSelectedConversation().messages().isEmpty());
  }

  private javax.swing.JButton findButton(Container container, String text) {
    javax.swing.JButton button = findButtonRecursively(container, text);
    assertNotNull(button);
    return button;
  }

  private javax.swing.JButton findButtonRecursively(Container container, String text) {
    for (Component component : container.getComponents()) {
      if (component instanceof javax.swing.JButton button && button.getText().equals(text)) {
        return button;
      }
      if (component instanceof Container nested) {
        javax.swing.JButton found = findButtonRecursively(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
