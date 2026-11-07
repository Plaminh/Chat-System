package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import com.chat.user.model.ChatHistoryActions;
import com.chat.user.model.ChatMessage;
import com.chat.user.model.Conversation;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class ChatHistorySearchPanel extends JPanel {
  public enum Scope {
    ALL("All conversations"),
    CURRENT("Current conversation");

    private final String label;

    Scope(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public record SearchResult(Conversation conversation, ChatMessage message) {}

  public record ActionConfirmation(
      ChatHistoryActions.Action action,
      int messageCount,
      List<String> conversationNames) {
    public ActionConfirmation {
      conversationNames = List.copyOf(conversationNames);
    }
  }

  private final List<Conversation> conversations;
  private final Conversation currentConversation;
  private final String currentUsername;
  private final Consumer<SearchResult> navigationHandler;
  private final Predicate<ActionConfirmation> confirmationHandler;
  private final ChatHistoryActions actions = new ChatHistoryActions();
  private final JTextField searchField = ChatTheme.createTextField(28);
  private final JComboBox<Scope> scope = new JComboBox<>(Scope.values());
  private final JPanel resultsPanel = new JPanel();
  private final Map<SearchResult, JCheckBox> selectionByResult = new LinkedHashMap<>();
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private final JButton unsendButton = ChatTheme.createDangerButton("Unsend Message");
  private List<SearchResult> visibleResults = List.of();
  private Runnable backHandler =
      () -> showFeedback("Return to the conversation from navigation.", ChatTheme.TEXT_MUTED);

  public ChatHistorySearchPanel(
      List<Conversation> conversations,
      Conversation currentConversation,
      String currentUsername,
      Consumer<SearchResult> navigationHandler,
      Predicate<ActionConfirmation> confirmationHandler) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    this.conversations = new ArrayList<>(Objects.requireNonNull(conversations, "conversations"));
    this.currentConversation = currentConversation;
    this.currentUsername = Objects.requireNonNull(currentUsername, "currentUsername");
    this.navigationHandler = Objects.requireNonNull(navigationHandler, "navigationHandler");
    this.confirmationHandler =
        Objects.requireNonNull(confirmationHandler, "confirmationHandler");
    buildContent();
    renderResults();
  }

  JTextField getSearchField() {
    return searchField;
  }

  JComboBox<Scope> getScopeSelector() {
    return scope;
  }

  List<SearchResult> getVisibleResults() {
    return visibleResults;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  javax.swing.JButton getUnsendButton() {
    return unsendButton;
  }

  public void setBackHandler(Runnable backHandler) {
    this.backHandler = Objects.requireNonNull(backHandler, "backHandler");
  }

  void selectResult(SearchResult result) {
    JCheckBox selected = selectionByResult.get(result);
    if (selected == null) {
      throw new IllegalArgumentException("Search result is not visible");
    }
    selected.setSelected(true);
    updateUnsendAvailability();
  }

  void applySelectedMessageAction(ChatHistoryActions.Action action) {
    if (action == ChatHistoryActions.Action.CLEAR_HISTORY) {
      throw new IllegalArgumentException("Use clearCurrentHistory for history removal");
    }
    Map<Conversation, Set<String>> selectedByConversation =
        selectionByResult.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .collect(
                Collectors.groupingBy(
                    entry -> entry.getKey().conversation(),
                    LinkedHashMap::new,
                    Collectors.mapping(
                        entry -> entry.getKey().message().messageId(), Collectors.toSet())));
    if (selectedByConversation.isEmpty()) {
      showFeedback("Select at least one message first.", ChatTheme.DANGER);
      return;
    }
    if (action == ChatHistoryActions.Action.UNSEND
        && selectionByResult.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .anyMatch(
                result ->
                    !result.message().senderUsername().equalsIgnoreCase(currentUsername))) {
      showFeedback(
          "Unsend is available only when every selected message was sent by you.",
          ChatTheme.DANGER);
      updateUnsendAvailability();
      return;
    }

    int messageCount =
        selectedByConversation.values().stream().mapToInt(Set::size).sum();
    ActionConfirmation confirmation =
        new ActionConfirmation(
            action,
            messageCount,
            selectedByConversation.keySet().stream().map(Conversation::displayName).toList());
    if (!confirmationHandler.test(confirmation)) {
      return;
    }

    selectedByConversation.forEach(
        (conversation, messageIds) ->
            actions.applyMessageAction(action, conversation, messageIds, currentUsername));
    showFeedback("Sample actions apply to this session only.", ChatTheme.TEXT_MUTED);
    renderResults();
  }

  void clearCurrentHistory() {
    if (currentConversation == null) {
      showFeedback("No conversation is selected.", ChatTheme.DANGER);
      return;
    }
    ActionConfirmation confirmation =
        new ActionConfirmation(
            ChatHistoryActions.Action.CLEAR_HISTORY,
            currentConversation.messages().size(),
            List.of(currentConversation.displayName()));
    if (!confirmationHandler.test(confirmation)) {
      return;
    }
    actions.clearHistory(currentConversation);
    showFeedback("Sample history was cleared from this session.", ChatTheme.TEXT_MUTED);
    renderResults();
  }

  private void buildContent() {
    setBackground(ChatTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));

    JLabel title = new JLabel("Search Message History");
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("Search messages across your sample conversations.");
    subtitle.setFont(ChatTheme.FONT_BODY);
    subtitle.setForeground(ChatTheme.TEXT_MUTED);
    JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, ChatTheme.SPACE_XS));
    titleBlock.setOpaque(false);
    titleBlock.add(title);
    titleBlock.add(subtitle);

    searchField.putClientProperty("JTextField.placeholderText", "Enter a message keyword");
    scope.setFont(ChatTheme.FONT_BODY);
    scope.setEnabled(currentConversation != null);
    scope.addActionListener(event -> renderResults());
    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, ChatTheme.SPACE_SM, 0));
    filters.setOpaque(false);
    filters.add(scope);
    filters.add(searchField);
    JButton deleteForMe = ChatTheme.createDangerButton("Delete for Me");
    deleteForMe.addActionListener(
        event -> applySelectedMessageAction(ChatHistoryActions.Action.DELETE_FOR_ME));
    unsendButton.addActionListener(
        event -> applySelectedMessageAction(ChatHistoryActions.Action.UNSEND));
    unsendButton.setToolTipText("Select only messages sent by you to enable Unsend.");
    JButton clearHistory = ChatTheme.createDangerButton("Clear Conversation History");
    clearHistory.addActionListener(event -> clearCurrentHistory());
    JButton back = ChatTheme.createSecondaryButton("Back to Chat");
    back.addActionListener(event -> backHandler.run());
    JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ChatTheme.SPACE_XS, 0));
    actionsRow.setOpaque(false);
    actionsRow.add(back);
    actionsRow.add(deleteForMe);
    actionsRow.add(unsendButton);
    actionsRow.add(clearHistory);

    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    heading.setOpaque(false);
    heading.add(titleBlock, BorderLayout.NORTH);
    JPanel controls = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    controls.setOpaque(false);
    controls.add(filters, BorderLayout.NORTH);
    controls.add(actionsRow, BorderLayout.SOUTH);
    heading.add(controls, BorderLayout.SOUTH);
    resultsPanel.setLayout(new javax.swing.BoxLayout(resultsPanel, javax.swing.BoxLayout.Y_AXIS));
    resultsPanel.setBackground(ChatTheme.CANVAS_BG);
    JScrollPane scroll = new JScrollPane(resultsPanel);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(ChatTheme.CANVAS_BG);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.TEXT_MUTED);
    add(heading, BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
    add(feedbackLabel, BorderLayout.SOUTH);
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        renderResults();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        renderResults();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        renderResults();
      }
    });
  }

  private void renderResults() {
    String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
    List<Conversation> searchConversations = conversations;
    if (scope.getSelectedItem() == Scope.CURRENT && currentConversation != null) {
      searchConversations = List.of(currentConversation);
    }
    visibleResults =
        query.isEmpty()
            ? List.of()
            : searchConversations.stream()
                .flatMap(
                    conversation ->
                        conversation.messages().stream()
                            .filter(message -> !message.removedForCurrentUser())
                            .filter(message -> message.displayContent().toLowerCase(Locale.ROOT)
                                .contains(query))
                            .map(message -> new SearchResult(conversation, message)))
                .toList();

    selectionByResult.clear();
    resultsPanel.removeAll();
    if (query.isEmpty()) {
      resultsPanel.add(emptyState("Enter a keyword to search message history."));
    } else if (visibleResults.isEmpty()) {
      resultsPanel.add(emptyState("No matching messages found."));
    } else {
      for (SearchResult result : visibleResults) {
        JCheckBox selected = new JCheckBox(result.message().displayContent());
        selected.setFont(ChatTheme.FONT_BODY);
        selected.setOpaque(false);
        selected.addActionListener(event -> updateUnsendAvailability());
        selectionByResult.put(result, selected);
        resultsPanel.add(createResultRow(result, selected));
        resultsPanel.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
      }
    }
    resultsPanel.revalidate();
    resultsPanel.repaint();
    updateUnsendAvailability();
  }

  private JPanel createResultRow(SearchResult result, JCheckBox selected) {
    JPanel card = ChatTheme.createCardPanel();
    card.setLayout(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    JLabel context =
        new JLabel(
            result.conversation().displayName()
                + "  •  "
                + ChatTheme.DATETIME_FMT.format(result.message().sentAt()));
    context.setFont(ChatTheme.FONT_SMALL);
    context.setForeground(ChatTheme.TEXT_MUTED);
    JButton navigate = ChatTheme.createPrimaryButton("Go to Message");
    navigate.addActionListener(event -> navigationHandler.accept(result));
    JPanel bottom = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    bottom.setOpaque(false);
    bottom.add(context, BorderLayout.CENTER);
    bottom.add(navigate, BorderLayout.EAST);
    card.add(selected, BorderLayout.CENTER);
    card.add(bottom, BorderLayout.SOUTH);
    return card;
  }

  private JLabel emptyState(String text) {
    JLabel label = new JLabel(text);
    label.setFont(ChatTheme.FONT_BODY);
    label.setForeground(ChatTheme.TEXT_MUTED);
    label.setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD));
    return label;
  }

  private void showFeedback(String message, java.awt.Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }

  private void updateUnsendAvailability() {
    List<SearchResult> selected =
        selectionByResult.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .toList();
    boolean allOwned =
        !selected.isEmpty()
            && selected.stream()
                .allMatch(
                    result ->
                        result.message().senderUsername().equalsIgnoreCase(currentUsername));
    unsendButton.setEnabled(allOwned);
    if (!selected.isEmpty() && !allOwned) {
      showFeedback(
          "Unsend is available only when every selected message was sent by you.",
          ChatTheme.DANGER);
    } else if (allOwned) {
      showFeedback("Selected messages were sent by you.", ChatTheme.TEXT_MUTED);
    } else {
      showFeedback("Select messages sent by you to enable Unsend.", ChatTheme.TEXT_MUTED);
    }
  }
}
