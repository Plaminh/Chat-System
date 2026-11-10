package com.chat.user.view;

import com.chat.user.component.MessageBubble;
import com.chat.user.ui.ChatTheme;
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
  private final JLabel encryptionPlaceholder = new JLabel();
  private Runnable backHandler =
      () -> {
        feedbackLabel.setText("Select Groups in the navigation to return.");
        feedbackLabel.setForeground(ChatTheme.TEXT_MUTED);
      };

  public GroupChatPanel(GroupConversation group, String currentUsername) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
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
      feedbackLabel.setText("Enter a message before continuing.");
      feedbackLabel.setForeground(ChatTheme.DANGER);
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
    feedbackLabel.setText("Sample group messages are kept in this session only.");
    feedbackLabel.setForeground(ChatTheme.TEXT_MUTED);
    renderMessages();
  }

  private void buildContent() {
    setBackground(ChatTheme.CANVAS_BG);
    setBorder(BorderFactory.createEmptyBorder(
        ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));

    JPanel chatPane = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    chatPane.setBackground(ChatTheme.CANVAS_BG);
    JPanel header = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    header.setBackground(ChatTheme.SURFACE_WHITE);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, ChatTheme.BORDER),
        BorderFactory.createEmptyBorder(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD,
            ChatTheme.SPACE_MD, ChatTheme.SPACE_MD)));
    JLabel groupName = new JLabel(group.name());
    groupName.setFont(ChatTheme.FONT_SUBTITLE);
    groupName.setForeground(ChatTheme.TEXT_PRIMARY);
    encryptionPlaceholder.setText(
        group.encrypted() ? "Encrypted Group (Prototype)" : "Standard Group");
    encryptionPlaceholder.setFont(ChatTheme.FONT_SMALL);
    encryptionPlaceholder.setForeground(
        group.encrypted() ? ChatTheme.PRIMARY : ChatTheme.TEXT_MUTED);
    JPanel groupInfo = new JPanel();
    groupInfo.setLayout(new javax.swing.BoxLayout(groupInfo, javax.swing.BoxLayout.Y_AXIS));
    groupInfo.setOpaque(false);
    groupName.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    encryptionPlaceholder.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    groupInfo.add(groupName);
    groupInfo.add(encryptionPlaceholder);
    if (group.encrypted()) {
      JLabel encryptionHint =
          new JLabel(
              "V1 display only; message encryption will be implemented in V2.");
      encryptionHint.setFont(ChatTheme.FONT_SMALL);
      encryptionHint.setForeground(ChatTheme.TEXT_MUTED);
      encryptionHint.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
      encryptionHint.setToolTipText(
          "Encryption is represented in this V1 prototype; message encryption will be implemented in V2.");
      groupInfo.add(encryptionHint);
    }
    header.add(groupInfo, BorderLayout.CENTER);
    JButton back = ChatTheme.createSecondaryButton("Back to Groups");
    back.addActionListener(event -> backHandler.run());
    JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ChatTheme.SPACE_XS, 0));
    headerActions.setOpaque(false);
    headerActions.add(back);
    header.add(headerActions, BorderLayout.EAST);

    messageList.setLayout(new javax.swing.BoxLayout(messageList, javax.swing.BoxLayout.Y_AXIS));
    messageList.setBackground(ChatTheme.CANVAS_BG);
    JScrollPane messageScroll = new JScrollPane(messageList);
    messageScroll.setBorder(BorderFactory.createEmptyBorder());
    messageScroll.getViewport().setBackground(ChatTheme.CANVAS_BG);

    composer.setFont(ChatTheme.FONT_BODY);
    composer.setLineWrap(true);
    composer.setWrapStyleWord(true);
    composer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ChatTheme.BORDER),
        BorderFactory.createEmptyBorder(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM,
            ChatTheme.SPACE_SM, ChatTheme.SPACE_SM)));
    JButton send = ChatTheme.createPrimaryButton("Send Sample");
    send.addActionListener(event -> sendSampleMessage());
    JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actionRow.setOpaque(false);
    actionRow.add(feedbackLabel);
    actionRow.add(send);
    JPanel composerPane = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    composerPane.setBackground(ChatTheme.SURFACE_WHITE);
    composerPane.setBorder(BorderFactory.createEmptyBorder(ChatTheme.SPACE_SM,
        ChatTheme.SPACE_MD, ChatTheme.SPACE_SM, ChatTheme.SPACE_MD));
    composerPane.add(composer, BorderLayout.CENTER);
    composerPane.add(actionRow, BorderLayout.SOUTH);

    memberList.setLayout(new javax.swing.BoxLayout(memberList, javax.swing.BoxLayout.Y_AXIS));
    memberList.setBackground(ChatTheme.SURFACE_WHITE);
    JPanel membersCard = ChatTheme.createCardPanel();
    membersCard.setLayout(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    JLabel membersTitle = new JLabel("Members & Roles");
    membersTitle.setFont(ChatTheme.FONT_SUBTITLE);
    membersTitle.setForeground(ChatTheme.TEXT_PRIMARY);
    membersCard.add(membersTitle, BorderLayout.NORTH);
    JScrollPane memberScroll = new JScrollPane(memberList);
    memberScroll.setBorder(BorderFactory.createEmptyBorder());
    memberScroll.getViewport().setBackground(ChatTheme.SURFACE_WHITE);
    membersCard.add(memberScroll, BorderLayout.CENTER);

    javax.swing.JSplitPane splitPane =
        new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, chatPane, membersCard);
    splitPane.setResizeWeight(0.78);
    splitPane.setDividerLocation(0.78);
    chatPane.setMinimumSize(new java.awt.Dimension(420, 0));
    membersCard.setMinimumSize(new java.awt.Dimension(180, 0));
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
      JLabel empty = new JLabel("No messages in this group yet.", SwingConstants.CENTER);
      empty.setFont(ChatTheme.FONT_BODY);
      empty.setForeground(ChatTheme.TEXT_MUTED);
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
      identity.setFont(ChatTheme.FONT_BODY_BOLD);
      JLabel role = new JLabel(
          member.role() == GroupMember.Role.ADMIN ? "Administrator" : "Member");
      role.setFont(ChatTheme.FONT_SMALL);
      role.setForeground(member.role() == GroupMember.Role.ADMIN
          ? ChatTheme.PRIMARY
          : ChatTheme.TEXT_MUTED);
      JPanel memberRow = new JPanel(new BorderLayout(ChatTheme.SPACE_XS, ChatTheme.SPACE_XS));
      memberRow.setOpaque(false);
      memberRow.setBorder(BorderFactory.createEmptyBorder(
          ChatTheme.SPACE_SM, 0, ChatTheme.SPACE_SM, 0));
      memberRow.add(identity, BorderLayout.NORTH);
      memberRow.add(role, BorderLayout.SOUTH);
      memberRow.setMaximumSize(
          new java.awt.Dimension(Integer.MAX_VALUE, memberRow.getPreferredSize().height));
      memberRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
      memberList.add(memberRow);
    }
    memberList.add(javax.swing.Box.createVerticalGlue());
    memberList.revalidate();
    memberList.repaint();
  }
}
