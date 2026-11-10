package com.chat.user.view;

import com.chat.common.ui.ChatTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class ChangePasswordDialog extends JPanel {
  private final JPasswordField currentPasswordField = new JPasswordField(24);
  private final JPasswordField newPasswordField = new JPasswordField(24);
  private final JPasswordField confirmPasswordField = new JPasswordField(24);
  private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
  private JDialog dialog;
  private boolean changeSubmitted;

  public ChangePasswordDialog() {
    super(new BorderLayout());
    buildContent();
  }

  public JDialog createDialog(Window owner) {
    dialog = new JDialog(owner, "Change Password", JDialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setMinimumSize(new Dimension(480, 370));
    dialog.setSize(480, 390);
    dialog.setLocationRelativeTo(owner);
    return dialog;
  }

  JPasswordField getCurrentPasswordField() {
    return currentPasswordField;
  }

  JPasswordField getNewPasswordField() {
    return newPasswordField;
  }

  JPasswordField getConfirmPasswordField() {
    return confirmPasswordField;
  }

  JLabel getFeedbackLabel() {
    return feedbackLabel;
  }

  boolean isChangeSubmitted() {
    return changeSubmitted;
  }

  void submitChange() {
    char[] current = currentPasswordField.getPassword();
    char[] next = newPasswordField.getPassword();
    char[] confirmation = confirmPasswordField.getPassword();
    boolean currentBlank = isBlank(current);
    boolean newBlank = isBlank(next);
    boolean confirmationBlank = isBlank(confirmation);
    boolean matches = Arrays.equals(next, confirmation);
    Arrays.fill(current, '\0');
    Arrays.fill(next, '\0');
    Arrays.fill(confirmation, '\0');
    currentPasswordField.setText("");
    newPasswordField.setText("");
    confirmPasswordField.setText("");

    if (currentBlank || newBlank || confirmationBlank) {
      showFeedback("Complete all password fields.", ChatTheme.DANGER);
      return;
    }
    if (!matches) {
      showFeedback("The new password and confirmation do not match.", ChatTheme.DANGER);
      return;
    }

    changeSubmitted = true;
    showFeedback(
        "Sample password change requested; the server password was not changed.",
        ChatTheme.SUCCESS);
  }

  private void buildContent() {
    setBackground(ChatTheme.SURFACE_WHITE);
    setBorder(
        BorderFactory.createEmptyBorder(
            ChatTheme.SPACE_LG, ChatTheme.SPACE_LG, ChatTheme.SPACE_MD, ChatTheme.SPACE_LG));

    JLabel title = new JLabel("Change Password", SwingConstants.CENTER);
    title.setFont(ChatTheme.FONT_TITLE);
    title.setForeground(ChatTheme.TEXT_PRIMARY);
    JPanel form = new JPanel();
    form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
    form.setOpaque(false);
    addField(form, "Current password", currentPasswordField);
    addField(form, "New password", newPasswordField);
    addField(form, "Confirm new password", confirmPasswordField);

    feedbackLabel.setFont(ChatTheme.FONT_SMALL);
    feedbackLabel.setForeground(ChatTheme.DANGER);
    JButton cancelButton = ChatTheme.createSecondaryButton("Cancel");
    cancelButton.addActionListener(event -> closeDialog());
    JButton submitButton = ChatTheme.createPrimaryButton("Confirm Sample Change");
    submitButton.addActionListener(event -> submitChange());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actions.setOpaque(false);
    actions.add(cancelButton);
    actions.add(submitButton);

    JPanel content = new JPanel(new BorderLayout(ChatTheme.SPACE_MD, ChatTheme.SPACE_MD));
    content.setOpaque(false);
    content.add(title, BorderLayout.NORTH);
    content.add(form, BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(ChatTheme.SPACE_SM, ChatTheme.SPACE_SM));
    footer.setOpaque(false);
    footer.add(feedbackLabel, BorderLayout.NORTH);
    footer.add(actions, BorderLayout.SOUTH);
    content.add(footer, BorderLayout.SOUTH);
    add(content, BorderLayout.CENTER);
  }

  private void addField(JPanel form, String labelText, JPasswordField field) {
    JLabel label = new JLabel(labelText);
    label.setFont(ChatTheme.FONT_BODY_BOLD);
    label.setForeground(ChatTheme.TEXT_SECONDARY);
    form.add(label);
    form.add(field);
    form.add(javax.swing.Box.createVerticalStrut(ChatTheme.SPACE_SM));
  }

  private void closeDialog() {
    if (dialog != null) {
      dialog.dispose();
    }
  }

  private boolean isBlank(char[] value) {
    for (char character : value) {
      if (!Character.isWhitespace(character)) {
        return false;
      }
    }
    return true;
  }

  private void showFeedback(String message, Color color) {
    feedbackLabel.setText(message);
    feedbackLabel.setForeground(color);
  }
}
