package com.chat.user.component;

import com.chat.user.model.ChatMessage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public final class MessageBubble extends JPanel {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  public MessageBubble(ChatMessage message, boolean ownMessage) {
    super(new FlowLayout(ownMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, UserUiTheme.SPACE_XS));
    putClientProperty("messageId", message.messageId());
    Color background = ownMessage ? UserUiTheme.PRIMARY : UserUiTheme.SURFACE_WHITE;
    Color foreground = ownMessage ? Color.WHITE : UserUiTheme.TEXT_PRIMARY;

    JPanel bubble = new JPanel(new BorderLayout(UserUiTheme.SPACE_SM, UserUiTheme.SPACE_SM));
    bubble.setBackground(background);
    bubble.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ownMessage ? UserUiTheme.PRIMARY : UserUiTheme.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 8, 12)));

    String content =
        message.unsent() ? "This message was unsent in the sample UI." : message.content();
    JTextArea text = new JTextArea(content);
    text.setEditable(false);
    text.setFocusable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setFont(UserUiTheme.FONT_BODY);
    text.setForeground(foreground);
    text.setBackground(background);
    text.setBorder(null);
    text.setColumns(42);
    text.setRows(Math.max(1, (content.length() / 42) + 1));
    text.setPreferredSize(new Dimension(420, text.getPreferredSize().height));

    JLabel timestamp = new JLabel(TIME_FORMAT.format(message.sentAt()));
    timestamp.setFont(UserUiTheme.FONT_SMALL);
    timestamp.setForeground(ownMessage ? new Color(0xDBEAFE) : UserUiTheme.TEXT_MUTED);
    JPanel metadata = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    metadata.setOpaque(false);
    metadata.add(timestamp);
    bubble.add(text, BorderLayout.CENTER);
    bubble.add(metadata, BorderLayout.SOUTH);
    add(bubble);
  }
}
