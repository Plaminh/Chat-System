package com.chat.user.view;

import com.chat.user.component.UserUiTheme;
import com.chat.user.model.ChatHistoryActions;
import com.chat.user.model.ChatMessage;
import com.chat.user.model.Conversation;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
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
    ALL("Tất cả cuộc trò chuyện"),
    CURRENT("Cuộc trò chuyện hiện tại");

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

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final List<Conversation> conversations;
  private final Conversation currentConversation;
  private final Consumer<SearchResult> navigationHandler;
  private final Predicate<ActionConfirmation> confirmationHandler;
  private final ChatHistoryActions actions = new ChatHistoryActions();
  private final JTextField searchField = UserUiTheme.createTextField(28);
  private final JComboBox<Scope> scope = new JComboBox<>(Scope.values());
  private final JPanel resultsPanel = new JPanel();
  private final Map<SearchResult, JCheckBox> selectionByResult = new LinkedHashMap<>();
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private List<SearchResult> visibleResults = List.of();

  public ChatHistorySearchPanel(
      List<Conversation> conversations,
      Conversation currentConversation,
      Consumer<SearchResult> navigationHandler,
      Predicate<ActionConfirmation> confirmationHandler) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.conversations = new ArrayList<>(Objects.requireNonNull(conversations, "conversations"));
    this.currentConversation = currentConversation;
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

  void selectResult(SearchResult result) {
    JCheckBox selected = selectionByResult.get(result);
    if (selected == null) {
      throw new IllegalArgumentException("Search result is not visible");
    }
    selected.setSelected(true);
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
      showFeedback("Chọn ít nhất một tin nhắn trước.", UserUiTheme.DANGER);
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
        (conversation, messageIds) -> actions.applyMessageAction(action, conversation, messageIds));
    showFeedback("Thao tác mẫu chỉ áp dụng trong phiên hiện tại.", UserUiTheme.TEXT_MUTED);
    renderResults();
  }

  void clearCurrentHistory() {
    if (currentConversation == null) {
      showFeedback("Không có cuộc trò chuyện được chọn.", UserUiTheme.DANGER);
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
    showFeedback("Lịch sử mẫu đã được xóa khỏi phiên hiện tại.", UserUiTheme.TEXT_MUTED);
    renderResults();
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));

    JLabel title = new JLabel("Tìm kiếm lịch sử");
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel subtitle = new JLabel("Tìm nội dung trong các cuộc trò chuyện mẫu.");
    subtitle.setFont(UserUiTheme.FONT_BODY);
    subtitle.setForeground(UserUiTheme.TEXT_MUTED);
    JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, UserUiTheme.SPACE_XS));
    titleBlock.setOpaque(false);
    titleBlock.add(title);
    titleBlock.add(subtitle);

    searchField.putClientProperty("JTextField.placeholderText", "Nhập từ khóa tìm tin nhắn");
    scope.setFont(UserUiTheme.FONT_BODY);
    scope.setEnabled(currentConversation != null);
    scope.addActionListener(event -> renderResults());
    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, UserUiTheme.SPACE_SM, 0));
    filters.setOpaque(false);
    filters.add(scope);
    filters.add(searchField);
    JButton deleteForMe = UserUiTheme.createSecondaryButton("Xóa tin nhắn cho tôi");
    deleteForMe.addActionListener(
        event -> applySelectedMessageAction(ChatHistoryActions.Action.DELETE_FOR_ME));
    JButton unsend = UserUiTheme.createSecondaryButton("Thu hồi tin nhắn");
    unsend.addActionListener(
        event -> applySelectedMessageAction(ChatHistoryActions.Action.UNSEND));
    JButton clearHistory = UserUiTheme.createSecondaryButton("Xóa lịch sử cuộc trò chuyện");
    clearHistory.addActionListener(event -> clearCurrentHistory());
    JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UserUiTheme.SPACE_XS, 0));
    actionsRow.setOpaque(false);
    actionsRow.add(deleteForMe);
    actionsRow.add(unsend);
    actionsRow.add(clearHistory);

    JPanel heading = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    heading.setOpaque(false);
    heading.add(titleBlock, BorderLayout.NORTH);
    JPanel controls = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    controls.setOpaque(false);
    controls.add(filters, BorderLayout.NORTH);
    controls.add(actionsRow, BorderLayout.SOUTH);
    heading.add(controls, BorderLayout.SOUTH);
    resultsPanel.setLayout(new javax.swing.BoxLayout(resultsPanel, javax.swing.BoxLayout.Y_AXIS));
    resultsPanel.setBackground(UserUiTheme.CANVAS_BG);
    JScrollPane scroll = new JScrollPane(resultsPanel);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(UserUiTheme.CANVAS_BG);

    feedbackLabel.setFont(UserUiTheme.FONT_SMALL);
    feedbackLabel.setForeground(UserUiTheme.TEXT_MUTED);
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
      resultsPanel.add(emptyState("Nhập từ khóa để tìm trong lịch sử."));
    } else if (visibleResults.isEmpty()) {
      resultsPanel.add(emptyState("Không tìm thấy tin nhắn phù hợp."));
    } else {
      for (SearchResult result : visibleResults) {
        JCheckBox selected = new JCheckBox(result.message().displayContent());
        selected.setFont(UserUiTheme.FONT_BODY);
        selected.setOpaque(false);
        selectionByResult.put(result, selected);
        resultsPanel.add(createResultRow(result, selected));
        resultsPanel.add(javax.swing.Box.createVerticalStrut(UserUiTheme.SPACE_SM));
      }
    }
    resultsPanel.revalidate();
    resultsPanel.repaint();
  }

  private JPanel createResultRow(SearchResult result, JCheckBox selected) {
    JPanel card = UserUiTheme.createCardPanel();
    card.setLayout(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    JLabel context =
        new JLabel(
            result.conversation().displayName()
                + "  •  "
                + DATE_FORMAT.format(result.message().sentAt()));
    context.setFont(UserUiTheme.FONT_SMALL);
    context.setForeground(UserUiTheme.TEXT_MUTED);
    JButton navigate = UserUiTheme.createPrimaryButton("Đi tới tin nhắn");
    navigate.addActionListener(event -> navigationHandler.accept(result));
    JPanel bottom = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    bottom.setOpaque(false);
    bottom.add(context, BorderLayout.CENTER);
    bottom.add(navigate, BorderLayout.EAST);
    card.add(selected, BorderLayout.CENTER);
    card.add(bottom, BorderLayout.SOUTH);
    return card;
  }

  private JLabel emptyState(String text) {
    JLabel label = new JLabel(text);
    label.setFont(UserUiTheme.FONT_BODY);
    label.setForeground(UserUiTheme.TEXT_MUTED);
    label.setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_LG, UserUiTheme.SPACE_MD));
    return label;
  }

  private void showFeedback(String message, java.awt.Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
