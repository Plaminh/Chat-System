package com.chat.user.view;

import com.chat.user.component.MessageBubble;
import com.chat.user.component.UserUiTheme;
import com.chat.user.model.ChatMessage;
import com.chat.user.model.GroupConversation;
import com.chat.user.model.GroupMember;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public final class GroupChatPanel extends JPanel {
  private final GroupConversation group;
  private final String currentUsername;
  private final JPanel messageList = new JPanel();
  private final JPanel memberList = new JPanel();
  private final JTextArea composer = new JTextArea(3, 32);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private final JLabel encryptionPlaceholder =
      new JLabel("Mã hóa: chỉ là nhãn giao diện mẫu, chưa được kích hoạt.");
  private Runnable backHandler =
      () -> {
        feedbackLabel.setText("Chọn Hội nhóm trong thanh điều hướng để quay lại.");
        feedbackLabel.setForeground(UserUiTheme.TEXT_MUTED);
      };

  public GroupChatPanel(GroupConversation group, String currentUsername) {
    super(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    this.group = Objects.requireNonNull(group, "group");
    this.currentUsername = Objects.requireNonNull(currentUsername, "currentUsername");
    buildContent();
    renderMessages();
    renderMembers();
  }

  JTextArea getComposer() {
    return composer;
  }

  JPanel getMessageList() {
    return messageList;
  }

  JPanel getMemberList() {
    return memberList;
  }

  JLabel getEncryptionPlaceholder() {
    return encryptionPlaceholder;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  public void setBackHandler(Runnable backHandler) {
    this.backHandler = Objects.requireNonNull(backHandler, "backHandler");
  }

  void sendSampleMessage() {
    String content = composer.getText().trim();
    if (content.isEmpty()) {
      feedbackLabel.setText("Nhập nội dung tin nhắn trước khi tiếp tục.");
      feedbackLabel.setForeground(UserUiTheme.DANGER);
      return;
    }

    group.addMessage(
        new ChatMessage(
            UUID.randomUUID().toString(),
            currentUsername,
            content,
            LocalDateTime.now(),
            false,
            false));
    composer.setText("");
    feedbackLabel.setText("Tin nhắn nhóm mẫu chỉ được giữ trong phiên này.");
    feedbackLabel.setForeground(UserUiTheme.TEXT_MUTED);
    renderMessages();
  }

  private void buildContent() {
    setBackground(UserUiTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));

    JPanel chatPane = new JPanel(new BorderLayout(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD));
    chatPane.setBackground(UserUiTheme.CANVAS_BG);
    JPanel header = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    header.setBackground(UserUiTheme.SURFACE_WHITE);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, UserUiTheme.BORDER),
        BorderFactory.createEmptyBorder(UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD,
            UserUiTheme.SPACE_MD, UserUiTheme.SPACE_MD)));
    JLabel groupName = new JLabel(group.name());
    groupName.setFont(UserUiTheme.FONT_SUBTITLE);
    groupName.setForeground(UserUiTheme.TEXT_PRIMARY);
    encryptionPlaceholder.setFont(UserUiTheme.FONT_SMALL);
    encryptionPlaceholder.setForeground(UserUiTheme.TEXT_MUTED);
    header.add(groupName, BorderLayout.WEST);
    JButton back = UserUiTheme.createSecondaryButton("Quay lại hội nhóm");
    back.addActionListener(event -> backHandler.run());
    JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UserUiTheme.SPACE_XS, 0));
    headerActions.setOpaque(false);
    headerActions.add(encryptionPlaceholder);
    headerActions.add(back);
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
    JButton send = UserUiTheme.createPrimaryButton("Gửi mẫu");
    send.addActionListener(event -> sendSampleMessage());
    JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actionRow.setOpaque(false);
    actionRow.add(feedbackLabel);
    actionRow.add(send);
    JPanel composerPane = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    composerPane.setBackground(UserUiTheme.SURFACE_WHITE);
    composerPane.setBorder(BorderFactory.createEmptyBorder(UserUiTheme.SPACE_SM,
        UserUiTheme.SPACE_MD, UserUiTheme.SPACE_SM, UserUiTheme.SPACE_MD));
    composerPane.add(composer, BorderLayout.CENTER);
    composerPane.add(actionRow, BorderLayout.SOUTH);

    memberList.setLayout(new javax.swing.BoxLayout(memberList, javax.swing.BoxLayout.Y_AXIS));
    memberList.setBackground(UserUiTheme.SURFACE_WHITE);
    JPanel membersCard = UserUiTheme.createCardPanel();
    membersCard.setLayout(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    JLabel membersTitle = new JLabel("Thành viên và vai trò");
    membersTitle.setFont(UserUiTheme.FONT_SUBTITLE);
    membersTitle.setForeground(UserUiTheme.TEXT_PRIMARY);
    membersCard.add(membersTitle, BorderLayout.NORTH);
    membersCard.add(memberList, BorderLayout.CENTER);

    javax.swing.JSplitPane splitPane =
        new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, chatPane, membersCard);
    splitPane.setResizeWeight(0.78);
    splitPane.setDividerLocation(820);
    splitPane.setBorder(null);
    chatPane.add(header, BorderLayout.NORTH);
    chatPane.add(messageScroll, BorderLayout.CENTER);
    chatPane.add(composerPane, BorderLayout.SOUTH);
    add(splitPane, BorderLayout.CENTER);
  }

  private void renderMessages() {
    messageList.removeAll();
    for (ChatMessage message : group.messages()) {
      if (!message.removedForCurrentUser()) {
        messageList.add(new MessageBubble(
            message, currentUsername.equalsIgnoreCase(message.senderUsername())));
      }
    }
    if (group.messages().isEmpty()) {
      JLabel empty = new JLabel("Chưa có tin nhắn trong nhóm này.", SwingConstants.CENTER);
      empty.setFont(UserUiTheme.FONT_BODY);
      empty.setForeground(UserUiTheme.TEXT_MUTED);
      messageList.add(empty);
    }
    messageList.add(javax.swing.Box.createVerticalGlue());
    messageList.revalidate();
    messageList.repaint();
  }

  private void renderMembers() {
    memberList.removeAll();
    for (GroupMember member : group.members()) {
      JLabel identity = new JLabel(
          member.user().fullName() + "  •  @" + member.user().username());
      identity.setFont(UserUiTheme.FONT_BODY_BOLD);
      JLabel role = new JLabel(
          member.role() == GroupMember.Role.ADMIN ? "Quản trị viên" : "Thành viên");
      role.setFont(UserUiTheme.FONT_SMALL);
      role.setForeground(member.role() == GroupMember.Role.ADMIN
          ? UserUiTheme.PRIMARY
          : UserUiTheme.TEXT_MUTED);
      JPanel memberRow = new JPanel(new BorderLayout(UserUiTheme.SPACE_XS, UserUiTheme.SPACE_XS));
      memberRow.setOpaque(false);
      memberRow.setBorder(BorderFactory.createEmptyBorder(
          UserUiTheme.SPACE_SM, 0, UserUiTheme.SPACE_SM, 0));
      memberRow.add(identity, BorderLayout.NORTH);
      memberRow.add(role, BorderLayout.SOUTH);
      memberList.add(memberRow);
    }
    memberList.revalidate();
    memberList.repaint();
  }
}
