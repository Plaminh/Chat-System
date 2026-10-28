package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.Conversation;
import java.util.List;
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
    assertTrue(panel.getFeedbackLabel().getText().contains("chưa được gửi"));
  }

  @Test
  void emptyComposerDoesNotChangeConversation() {
    List<Conversation> conversations = ChatWorkspacePanel.createSampleConversations();
    ChatWorkspacePanel panel = new ChatWorkspacePanel(conversations, "alex.morgan");
    int initialCount = panel.getSelectedConversation().messages().size();

    panel.sendSampleMessage();

    assertEquals(initialCount, panel.getSelectedConversation().messages().size());
    assertTrue(panel.getFeedbackLabel().getText().contains("Nhập nội dung"));
  }
}
