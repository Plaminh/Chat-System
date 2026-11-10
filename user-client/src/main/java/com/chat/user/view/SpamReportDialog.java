package com.chat.user.view;

import com.chat.user.ui.ChatTheme;
import com.chat.user.model.UserProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class SpamReportDialog extends JPanel {
  private final UserProfile reportedUser;
  private final boolean hasPriorConversation;
  private final JTextArea reasonField = new JTextArea(5, 36);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.LEFT);
  private JDialog dialog;
  private boolean reportSubmitted;

  public SpamReportDialog(UserProfile reportedUser, boolean hasPriorConversation) {
    super(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    this.reportedUser = Objects.requireNonNull(reportedUser, "reportedUser");
    this.hasPriorConversation = hasPriorConversation;
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Report Account", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(560, 420));
    dialog.setSize(560, 460);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextArea getReasonField() {
    return reasonField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isReportSubmitted() {
    return reportSubmitted;
  }

  void submitReport() {
    if (!hasPriorConversation) {
      showFeedback(
          "You can only report an account after a conversation has taken place.",
          ChatTheme.DANGER);
      return;
    }
    if (reasonField.getText().trim().isEmpty()) {
      showFeedback("Enter a reason for the report before continuing.", ChatTheme.DANGER);
      return;
    }
    reportSubmitted = true;
    showFeedback(
        "Sample report recorded in the UI; it was not sent to the server.",
        ChatTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));
    JLabel title = new JLabel("Report Conversation", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel target =
        new JLabel(
            "Reported account: "
                + reportedUser.fullName()
                + "  •  @"
                + reportedUser.username());
    target.setFont(ChatTheme.FONT_BODY_BOLD);
    target.setForeground(ChatTheme.TEXT_SECONDARY);
    JLabel reasonLabel = new JLabel("Reason for report");
    reasonLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    reasonLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    reasonField.setFont(ChatTheme.FONT_BODY);
    reasonField.setLineWrap(true);
    reasonField.setWrapStyleWord(true);
    reasonField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ChatTheme.BORDER),
        BorderFactory.createEmptyBorder(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM,
            ChatTheme.SPACE_SM, ChatTheme.SPACE_SM)));

    JPanel form = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    form.setOpaque(false);
    form.add(target, BorderLayout.NORTH);
    JPanel reasonGroup = new JPanel(new BorderLayout(ChatTheme.SPACE_XS, ChatTheme.SPACE_XS));
    reasonGroup.setOpaque(false);
    reasonGroup.add(reasonLabel, BorderLayout.NORTH);
    reasonGroup.add(new JScrollPane(reasonField), BorderLayout.CENTER);
    form.add(reasonGroup, BorderLayout.CENTER);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);
    JButton cancel = ChatTheme.createSecondaryButton("Cancel");
    cancel.addActionListener(event -> closeDialog());
    JButton submit = ChatTheme.createPrimaryButton("Submit Sample Report");
    submit.addActionListener(event -> submitReport());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(feedbackLabel);
    actions.add(cancel);
    actions.add(submit);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    content.add(actions, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
