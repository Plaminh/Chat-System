package com.chat.user.view;

import com.chat.user.component.MessageBubble;
import com.chat.user.component.UserUiTheme;
import com.chat.user.model.ChatMessage;
import com.chat.user.model.Conversation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class ChatWorkspacePanel extends JPanel {
  private final List<Conversation> conversations;
  private final String currentUsername;
  private final DefaultListModel<Conversation> conversationListModel = new DefaultListModel<>();
  private final JList<Conversation> conversationList = new JList<>(conversationListModel);
  private final javax.swing.JTextField conversationFilter = UserUiTheme.createTextField(22);
  private final JPanel messageList = new JPanel();
  private final JTextArea composer = new JTextArea(3, 32);
  private final JLabel conversationTitle = new JLabel("Cuộc trò chuyện");
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private Conversation selectedConversation;
  private Runnable historySearchHandler = this::showUnavailableAction;
  private Consumer<Conversation> reportHandler = conversation -> showUnavailableAction();
  private Consumer<String> writingAssistHandler = draft -> showUnavailableAction();

  public ChatWorkspacePanel(List<Conversation> conversations, String currentUsername) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.conversations = new ArrayList<>(Objects.requireNonNull(conversations, "conversations"));
    this.currentUsername = Objects.requireNonNull(currentUsername, "currentUsername");
    buildContent();
    renderConversationList();
    if (!conversationListModel.isEmpty()) {
      conversationList.setSelectedIndex(0);
    } else {
      renderMessages();
    }
  }

  public static List<Conversation> createSampleConversations() {
    return List.of(
        new Conversation(
            "direct-mai-anh",
            "mai.anh",
            "Lê Mai Anh",
            List.of(
                new ChatMessage(
                    "message-1",
                    "mai.anh",
                    "Chào bạn! Mình đã gửi nội dung tài liệu mẫu.",
                    LocalDateTime.of(2026, 10, 27, 10, 32),
                    false,
                    false),
                new ChatMessage(
                    "message-2",
                    "alex.morgan",
                    "Cảm ơn bạn, mình sẽ xem và phản hồi sớm.",
                    LocalDateTime.of(2026, 10, 27, 10, 38),
                    false,
                    false))),
        new Conversation(
            "direct-minh-tran",
            "minh.tran",
            "Trần Minh",
            List.of(
                new ChatMessage(
                    "message-3",
                    "minh.tran",
                    "Cuộc hẹn của chúng ta vẫn vào chiều nay nhé?",
                    LocalDateTime.of(2026, 10, 26, 15, 5),
                    false,
                    false))));
  }

  JTextArea getComposer() {
    return composer;
  }

  Conversation getSelectedConversation() {
    return selectedConversation;
  }

  List<Conversation> getConversations() {
    return List.copyOf(conversations);
  }

  JPanel getMessageList() {
    return messageList;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  public void setActionHandlers(
      Runnable historySearchHandler,
      Consumer<Conversation> reportHandler,
      Consumer<String> writingAssistHandler) {
    this.historySearchHandler = Objects.requireNonNull(historySearchHandler, "historySearchHandler");
    this.reportHandler = Objects.requireNonNull(reportHandler, "reportHandler");
    this.writingAssistHandler = Objects.requireNonNull(writingAssistHandler, "writingAssistHandler");
  }

  public void openConversationFor(String username, String displayName) {
    Conversation conversation =
        conversations.stream()
            .filter(candidate -> candidate.username().equalsIgnoreCase(username))
            .findFirst()
            .orElseGet(
                () -> {
                  Conversation created =
                      new Conversation(
                          "direct-" + UUID.randomUUID(), username, displayName, List.of());
                  conversations.add(created);
                  return created;
                });
    renderConversationList();
    conversationList.setSelectedValue(conversation, true);
  }

  public void openSearchResult(ChatHistorySearchPanel.SearchResult result) {
    Objects.requireNonNull(result, "result");
    openConversationFor(result.conversation().username(), result.conversation().displayName());
    for (java.awt.Component component : messageList.getComponents()) {
      if (component instanceof MessageBubble bubble
          && result.message().messageId().equals(bubble.getClientProperty("messageId"))) {
        messageList.scrollRectToVisible(bubble.getBounds());
        break;
      }
    }
  }

  public void setDraftText(String draft) {
    composer.setText(Objects.requireNonNull(draft, "draft"));
  }

  public String getDraftText() {
    return composer.getText();
  }

  void sendSampleMessage() {
    String content = composer.getText().trim();
    if (content.isEmpty()) {
      feedbackLabel.setText("Nhập nội dung tin nhắn trước khi tiếp tục.");
      feedbackLabel.setForeground(UserUiTheme.DANGER);
      return;
    }
    if (selectedConversation == null) {
      feedbackLabel.setText("Chọn một cuộc trò chuyện trước khi soạn tin nhắn.");
      feedbackLabel.setForeground(UserUiTheme.DANGER);
      return;
    }

    selectedConversation.addMessage(
        new ChatMessage(
            UUID.randomUUID().toString(),
            currentUsername,
            content,
            LocalDateTime.now(),
            false,
            false));
    composer.setText("");
    feedbackLabel.setText("Tin nhắn mẫu đã thêm vào phiên này; chưa được gửi đi.");
    feedbackLabel.setForeground(UserUiTheme.TEXT_MUTED);
    renderMessages();
    renderConversationList();
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));

    JPanel conversationPane = buildConversationPane();
    JPanel chatPane = buildChatPane();
    javax.swing.JSplitPane splitPane =
        new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, conversationPane, chatPane);
    splitPane.setResizeWeight(0.30);
    splitPane.setDividerLocation(300);
    splitPane.setBorder(null);
    add(splitPane, BorderLayout.CENTER);
  }

  private JPanel buildConversationPane() {
    JPanel pane = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    pane.setBackground(UserUiTheme.SURFACE_WHITE);
    pane.setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    JLabel title = new JLabel("Đoạn trò chuyện");
    title.setFont(UserUiTheme.FONT_TITLE);
    title.setForeground(UserUiTheme.TEXT_PRIMARY);
    conversationFilter.putClientProperty("JTextField.placeholderText", "Tìm cuộc trò chuyện");
    JPanel heading = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(conversationFilter, BorderLayout.SOUTH);

    conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    conversationList.setCellRenderer(new ConversationCellRenderer());
    conversationList.setBackground(UserUiTheme.SURFACE_WHITE);
    conversationList.setFixedCellHeight(64);
    conversationList.addListSelectionListener(
        event -> {
          if (!event.getValueIsAdjusting()) {
            selectedConversation = conversationList.getSelectedValue();
            renderMessages();
          }
        });
    conversationFilter.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent event) {
        renderConversationList();
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        renderConversationList();
      }

      @Override
      public void changedUpdate(DocumentEvent event) {
        renderConversationList();
      }
    });

    pane.add(heading, BorderLayout.NORTH);
    pane.add(new JScrollPane(conversationList), BorderLayout.CENTER);
    return pane;
  }

  private JPanel buildChatPane() {
    JPanel pane = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    pane.setBackground(UserUiTheme.CANVAS_BG);
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(UserUiTheme.SURFACE_WHITE);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, UserUiTheme.BORDER),
        BorderFactory.createEmptyBorder(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD,
            UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD)));
    conversationTitle.setFont(UserUiTheme.FONT_SUBTITLE);
    conversationTitle.setForeground(UserUiTheme.TEXT_PRIMARY);
    JLabel sampleBadge = new JLabel("Bản mẫu");
    sampleBadge.setFont(UserUiTheme.FONT_SMALL);
    sampleBadge.setForeground(UserUiTheme.TEXT_MUTED);
    JButton historySearch = UserUiTheme.createSecondaryButton("Tìm lịch sử");
    historySearch.addActionListener(event -> historySearchHandler.run());
    JButton report = UserUiTheme.createSecondaryButton("Báo cáo");
    report.addActionListener(
        event -> {
          if (selectedConversation == null) {
            feedbackLabel.setText("Chọn cuộc trò chuyện trước khi báo cáo.");
            return;
          }
          reportHandler.accept(selectedConversation);
        });
    JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UserUiTheme.SPACE_XS, 0));
    headerActions.setOpaque(false);
    headerActions.add(sampleBadge);
    headerActions.add(historySearch);
    headerActions.add(report);
    header.add(conversationTitle, BorderLayout.WEST);
    header.add(headerActions, BorderLayout.EAST);

    messageList.setLayout(new javax.swing.BoxLayout(messageList, javax.swing.BoxLayout.Y_AXIS));
    messageList.setBackground(UserUiTheme.CANVAS_BG);
    JScrollPane messageScroll = new JScrollPane(messageList);
    messageScroll.setBorder(BorderFactory.createEmptyBorder());
    messageScroll.getViewport().setBackground(UserUiTheme.CANVAS_BG);

    composer.setFont(UserUiTheme.FONT_BODY);
    composer.setLineWrap(true);
    composer.setWrapStyleWord(true);
    composer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UserUiTheme.BORDER),
        BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM,
            UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM)));
    composer.setText("");
    JButton sendButton = UserUiTheme.createPrimaryButton("Gửi mẫu");
    sendButton.addActionListener(event -> sendSampleMessage());
    JButton assistButton = UserUiTheme.createSecondaryButton("Trợ lý soạn thảo");
    assistButton.addActionListener(event -> writingAssistHandler.accept(composer.getText()));
    JPanel composeActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    composeActions.setOpaque(false);
    composeActions.add(feedbackLabel);
    composeActions.add(assistButton);
    composeActions.add(sendButton);

    JPanel composerPane = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    composerPane.setBackground(UserUiTheme.SURFACE_WHITE);
    composerPane.setBorder(BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM,
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_SM, UserUiTheme.SPACE_MD));
    composerPane.add(composer, BorderLayout.CENTER);
    composerPane.add(composeActions, BorderLayout.SOUTH);

    pane.add(header, BorderLayout.NORTH);
    pane.add(messageScroll, BorderLayout.CENTER);
    pane.add(composerPane, BorderLayout.SOUTH);
    return pane;
  }

  private void renderConversationList() {
    String filter = conversationFilter.getText().trim().toLowerCase(java.util.Locale.ROOT);
    Conversation previous = selectedConversation;
    conversationListModel.clear();
    for (Conversation conversation : conversations) {
      if (conversation.displayName().toLowerCase(java.util.Locale.ROOT).contains(filter)
          || conversation.username().toLowerCase(java.util.Locale.ROOT).contains(filter)) {
        conversationListModel.addElement(conversation);
      }
    }
    if (previous != null) {
      conversationList.setSelectedValue(previous, true);
    }
    if (conversationListModel.isEmpty()) {
      selectedConversation = null;
      renderMessages();
    }
  }

  private void showUnavailableAction() {
    feedbackLabel.setText("Tác vụ này chưa được kết nối.");
    feedbackLabel.setForeground(UserUiTheme.TEXT_MUTED);
  }

  private void renderMessages() {
    messageList.removeAll();
    if (selectedConversation == null) {
      conversationTitle.setText("Cuộc trò chuyện");
      JLabel empty = new JLabel("Chọn một cuộc trò chuyện để xem tin nhắn.");
      empty.setFont(UserUiTheme.FONT_BODY);
      empty.setForeground(UserUiTheme.TEXT_MUTED);
      empty.setAlignmentX(CENTER_ALIGNMENT);
      messageList.add(empty);
    } else {
      conversationTitle.setText(selectedConversation.displayName());
      for (ChatMessage message : selectedConversation.messages()) {
        if (!message.removedForCurrentUser()) {
          messageList.add(new MessageBubble(
              message, currentUsername.equalsIgnoreCase(message.senderUsername())));
        }
      }
      if (selectedConversation.messages().isEmpty()) {
        JLabel empty = new JLabel("Chưa có tin nhắn trong cuộc trò chuyện này.");
        empty.setFont(UserUiTheme.FONT_BODY);
        empty.setForeground(UserUiTheme.TEXT_MUTED);
        messageList.add(empty);
      }
    }
    messageList.add(javax.swing.Box.createVerticalGlue());
    messageList.revalidate();
    messageList.repaint();
  }

  private static final class ConversationCellRenderer extends DefaultListCellRenderer {
    @Override
    public java.awt.Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Conversation conversation = (Conversation) value;
      String preview =
          conversation.messages().isEmpty()
              ? "Chưa có tin nhắn"
              : conversation.messages().get(conversation.messages().size() - 1).content();
      JLabel label = new JLabel(
          "<html><b>" + escapeHtml(conversation.displayName()) + "</b><br>"
              + escapeHtml(preview) + "</html>");
      label.setPreferredSize(new Dimension(240, 56));
      label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
      label.setFont(UserUiTheme.FONT_BODY);
      label.setForeground(UserUiTheme.TEXT_PRIMARY);
      label.setBackground(isSelected ? UserUiTheme.PRIMARY_SOFT_BG : UserUiTheme.SURFACE_WHITE);
      label.setOpaque(true);
      return label;
    }
  }

  private static String escapeHtml(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
