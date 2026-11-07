package com.chat.user.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.chat.user.model.ChatHistoryActions;
import com.chat.user.model.Conversation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatHistorySearchPanelTest {
  @Test
  void searchesAcrossConversationsAndCanNavigateToResult() {
    List<Conversation> conversations =
        new ArrayList<>(ChatWorkspacePanel.createSampleConversations());
    List<ChatHistorySearchPanel.SearchResult> navigated = new ArrayList<>();
    ChatHistorySearchPanel panel =
        new ChatHistorySearchPanel(
            conversations, conversations.get(0), "alex.morgan", navigated::add, request -> true);
    panel.getSearchField().setText("sample document");

    assertTrue(panel.getVisibleResults().size() >= 1);
    ChatHistorySearchPanel.SearchResult result = panel.getVisibleResults().get(0);
    panel.selectResult(result);
    clickButton(panel, "Go to Message");
    assertEquals(result, navigated.get(0));
  }

  @Test
  void deleteForMeAndUnsendAreDifferentLocalStates() {
    List<Conversation> conversations =
        new ArrayList<>(ChatWorkspacePanel.createSampleConversations());
    Conversation conversation = conversations.get(0);
    ChatHistorySearchPanel panel =
        new ChatHistorySearchPanel(
            conversations, conversation, "alex.morgan", result -> {}, request -> true);
    panel.getSearchField().setText("sample document");
    ChatHistorySearchPanel.SearchResult first = panel.getVisibleResults().get(0);
    panel.selectResult(first);
    panel.applySelectedMessageAction(ChatHistoryActions.Action.DELETE_FOR_ME);
    assertTrue(
        conversation.messages().stream()
            .filter(message -> message.messageId().equals(first.message().messageId()))
            .findFirst()
            .orElseThrow()
            .removedForCurrentUser());

    panel.getSearchField().setText("Thanks");
    ChatHistorySearchPanel.SearchResult second = panel.getVisibleResults().get(0);
    panel.selectResult(second);
    panel.applySelectedMessageAction(ChatHistoryActions.Action.UNSEND);
    assertTrue(
        conversation.messages().stream()
            .filter(message -> message.messageId().equals(second.message().messageId()))
            .findFirst()
            .orElseThrow()
            .unsent());
    assertFalse(second.message().unsent());
  }

  @Test
  void cancelledClearHistoryLeavesMessagesUnchanged() {
    List<Conversation> conversations =
        new ArrayList<>(ChatWorkspacePanel.createSampleConversations());
    Conversation conversation = conversations.get(0);
    int initialCount = conversation.messages().size();
    ChatHistorySearchPanel panel =
        new ChatHistorySearchPanel(
            conversations, conversation, "alex.morgan", result -> {}, request -> false);

    panel.clearCurrentHistory();

    assertEquals(initialCount, conversation.messages().size());
  }

  @Test
  void unsendIsDisabledForOtherUsersMessagesAndMixedSelectionIsRejected() {
    List<Conversation> conversations =
        new ArrayList<>(ChatWorkspacePanel.createSampleConversations());
    Conversation conversation = conversations.get(0);
    conversation.addMessage(
        new com.chat.user.model.ChatMessage(
            "own-sample",
            "alex.morgan",
            "sample reply",
            java.time.LocalDateTime.now(),
            false,
            false));
    ChatHistorySearchPanel panel =
        new ChatHistorySearchPanel(
            conversations, conversation, "alex.morgan", result -> {}, request -> true);
    panel.getSearchField().setText("sample");

    assertFalse(panel.getUnsendButton().isEnabled());
    panel.selectResult(
        panel.getVisibleResults().stream()
            .filter(result -> result.message().senderUsername().equals("mai.anh"))
            .findFirst()
            .orElseThrow());
    assertFalse(panel.getUnsendButton().isEnabled());
    panel.selectResult(
        panel.getVisibleResults().stream()
            .filter(result -> result.message().senderUsername().equals("alex.morgan"))
            .findFirst()
            .orElseThrow());
    assertFalse(panel.getUnsendButton().isEnabled());
    assertTrue(panel.getFeedbackLabel().getText().contains("only when every selected message"));
  }

  private javax.swing.JButton findButton(java.awt.Container container, String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JButton button && button.getText().equals(text)) {
        return button;
      }
      if (component instanceof java.awt.Container nested) {
        javax.swing.JButton found = findButton(nested, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private void clickButton(java.awt.Container container, String text) {
    javax.swing.JButton button = findButton(container, text);
    if (button == null) {
      throw new AssertionError("Button not found: " + text);
    }
    button.doClick();
  }
}
