package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class PasswordRecoveryDialog extends JPanel {
  private final JTextField emailField = ChatTheme.createTextField(30);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private JDialog dialog;
  private boolean requestSubmitted;

  public PasswordRecoveryDialog() {
    super(new BorderLayout());
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Password Recovery", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(520, 300));
    dialog.setSize(520, 320);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JTextField getEmailField() {
    return emailField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isRequestSubmitted() {
    return requestSubmitted;
  }

  void submitRecoveryRequest() {
    String email = emailField.getText().trim();
    if (email.isEmpty()) {
      showFeedback("Enter your email address.", ChatTheme.DANGER);
      return;
    }
    if (!isValidEmail(email)) {
      showFeedback("Enter a valid email address.", ChatTheme.DANGER);
      return;
    }

    requestSubmitted = true;
    showFeedback(
        "Sample request recorded; no email or password was sent.",
        ChatTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));

    JLabel title = new JLabel("Password Recovery", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JLabel description =
        new JLabel(
            "Enter the email linked to your account to view a sample response.",
            SwingConstants.CENTER);
    description.setFont(ChatTheme.FONT_BODY);
    description.setForeground(ChatTheme.TEXT_MUTED);

    JLabel emailLabel = new JLabel("Email address");
    emailLabel.setFont(ChatTheme.FONT_BODY_BOLD);
    emailLabel.setForeground(ChatTheme.TEXT_SECONDARY);
    JPanel form = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    form.setOpaque(false);
    form.add(emailLabel, BorderLayout.NORTH);
    form.add(emailField, BorderLayout.CENTER);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);

    JButton cancelButton = ChatTheme.createSecondaryButton("Back to Sign In");
    cancelButton.addActionListener(event -> closeDialog());
    JButton submitButton = ChatTheme.createPrimaryButton("Send Sample Request");
    submitButton.addActionListener(event -> submitRecoveryRequest());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, ChatTheme.SPACE_SM, 0));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    JPanel heading = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    heading.setOpaque(false);
    heading.add(title, BorderLayout.NORTH);
    heading.add(description, BorderLayout.SOUTH);
    content.add(heading, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);

    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
